package com.rizzleworks.discordbot.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.rizzleworks.discordbot.NotificationEvent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RzlDiscordCommandTest {

    @Mock private JavaPlugin plugin;
    @Mock private FileConfiguration config;
    @Mock private Commands commands;
    @Mock private CommandContext<CommandSourceStack> ctx;
    @Mock private CommandSourceStack source;
    @Mock private CommandSender sender;

    private LiteralCommandNode<CommandSourceStack> root;

    @BeforeEach
    void setUp() {
        new RzlDiscordCommand(plugin).register(commands);

        ArgumentCaptor<LiteralCommandNode<CommandSourceStack>> captor = ArgumentCaptor.captor();
        verify(commands).register(captor.capture(), anyString());
        root = captor.getValue();
    }

    @Test
    void registersTheExpectedTree() {
        assertThat(root.getName()).isEqualTo(RzlDiscordCommand.COMMAND_NAME);
        assertThat(root.getChildren()).extracting(CommandNode::getName)
                .containsExactlyInAnyOrder("toggle", "status", "reload");
    }

    @Test
    void permittedSenderPassesTheRequirement() {
        when(source.getSender()).thenReturn(sender);
        when(sender.hasPermission(RzlDiscordCommand.PERMISSION)).thenReturn(true);

        assertThat(root.getRequirement().test(source))
                .as("sender with %s should pass", RzlDiscordCommand.PERMISSION)
                .isTrue();
    }

    @Test
    void unpermittedSenderFailsTheRequirement() {
        when(source.getSender()).thenReturn(sender);
        when(sender.hasPermission(RzlDiscordCommand.PERMISSION)).thenReturn(false);

        assertThat(root.getRequirement().test(source))
                .as("sender without %s should be rejected", RzlDiscordCommand.PERMISSION)
                .isFalse();
    }

    @Test
    void toggleDisablesAnEnabledEvent() throws Exception {
        withSender();
        when(plugin.getConfig()).thenReturn(config);
        when(ctx.getArgument("event", String.class)).thenReturn("death");
        when(config.getBoolean(NotificationEvent.DEATH.toggleKey(), true)).thenReturn(true);

        assertThat(run(toggleArgument())).isEqualTo(Command.SINGLE_SUCCESS);

        verify(config).set(NotificationEvent.DEATH.toggleKey(), false);
        verify(plugin).saveConfig();
        assertThat(messages()).containsExactly("death notifications disabled");
    }

    @Test
    void toggleEnablesADisabledEvent() throws Exception {
        withSender();
        when(plugin.getConfig()).thenReturn(config);
        when(ctx.getArgument("event", String.class)).thenReturn("join");
        when(config.getBoolean(NotificationEvent.JOIN.toggleKey(), true)).thenReturn(false);

        run(toggleArgument());

        verify(config).set(NotificationEvent.JOIN.toggleKey(), true);
        verify(plugin).saveConfig();
        assertThat(messages()).containsExactly("join notifications enabled");
    }

    @Test
    void toggleAcceptsAnyCase() throws Exception {
        withSender();
        when(plugin.getConfig()).thenReturn(config);
        when(ctx.getArgument("event", String.class)).thenReturn("ADVANCEMENT");
        when(config.getBoolean(NotificationEvent.ADVANCEMENT.toggleKey(), true)).thenReturn(true);

        run(toggleArgument());

        verify(config).set(NotificationEvent.ADVANCEMENT.toggleKey(), false);
        // the reply echoes what was typed, not the canonical short name
        assertThat(messages()).containsExactly("ADVANCEMENT notifications disabled");
    }

    @Test
    void toggleRejectsAnUnknownEvent() throws Exception {
        withSender();
        when(ctx.getArgument("event", String.class)).thenReturn("bogus");

        assertThat(run(toggleArgument())).isEqualTo(Command.SINGLE_SUCCESS);

        verify(plugin, never()).saveConfig();
        assertThat(messages()).containsExactly(
                "Unknown event: bogus. Options: join, leave, death, advancement");
    }

    @Test
    void statusListsEveryEvent() throws Exception {
        withSender();
        when(plugin.getConfig()).thenReturn(config);
        when(config.getBoolean(NotificationEvent.JOIN.toggleKey(), true)).thenReturn(true);
        when(config.getBoolean(NotificationEvent.LEAVE.toggleKey(), true)).thenReturn(false);
        when(config.getBoolean(NotificationEvent.DEATH.toggleKey(), true)).thenReturn(true);
        when(config.getBoolean(NotificationEvent.ADVANCEMENT.toggleKey(), true)).thenReturn(false);

        assertThat(run(root.getChild("status"))).isEqualTo(Command.SINGLE_SUCCESS);

        assertThat(messages()).containsExactly(
                "--- RzlDiscordBot Event Status ---",
                "  join: enabled",
                "  leave: disabled",
                "  death: enabled",
                "  advancement: disabled");
    }

    @Test
    void reloadRereadsTheConfig() throws Exception {
        withSender();

        assertThat(run(root.getChild("reload"))).isEqualTo(Command.SINGLE_SUCCESS);

        verify(plugin).reloadConfig();
        assertThat(messages()).containsExactly("Configuration reloaded.");
    }

    @Test
    void suggestsEveryEventName() throws Exception {
        String typed = "rzldiscord toggle ";
        var suggestions = toggleArgument().getCustomSuggestions()
                .getSuggestions(ctx, new SuggestionsBuilder(typed, typed.length()))
                .get();

        assertThat(suggestions.getList()).extracting(Suggestion::getText)
                .containsExactlyInAnyOrderElementsOf(NotificationEvent.SHORT_NAMES);
    }

    private void withSender() {
        when(ctx.getSource()).thenReturn(source);
        when(source.getSender()).thenReturn(sender);
    }

    private int run(CommandNode<CommandSourceStack> node) throws Exception {
        return node.getCommand().run(ctx);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCommandNode<CommandSourceStack, String> toggleArgument() {
        return (ArgumentCommandNode<CommandSourceStack, String>) root.getChild("toggle").getChild("event");
    }

    private List<String> messages() {
        ArgumentCaptor<Component> captor = ArgumentCaptor.captor();
        verify(sender, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues().stream()
                .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                .toList();
    }
}
