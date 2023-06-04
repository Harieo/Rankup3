package sh.okx.rankup.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.messages.Message;
import sh.okx.rankup.messages.MessageBuilder;
import sh.okx.rankup.prestige.Prestige;
import sh.okx.rankup.prestige.Prestiges;
import sh.okx.rankup.ranks.RankElement;

@RequiredArgsConstructor
public class PrestigesCommand implements CommandExecutor {
  private final RankupPlugin plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (plugin.error(sender)) {
      return true;
    }

    Prestiges prestiges = plugin.getPrestiges();
    Prestige playerRank = null;
    if (sender instanceof Player) {
      playerRank = prestiges.getRankByPlayer((Player) sender);
    }

    plugin.sendHeaderFooter(sender, playerRank, Message.PRESTIGES_HEADER);

    Message message = playerRank == null ? Message.PRESTIGES_INCOMPLETE : Message.PRESTIGES_COMPLETE;
    RankElement<Prestige> prestige;
    if (sender instanceof Player) {
      Player player = (Player) sender;
      prestige = prestiges.findTrackOrDefault(player).getFirst();
    } else if (args.length > 0) { // The console has specified a track which can be retrieved
      String rankName = args[0];
      prestige = prestiges.getByName(rankName).orElse(null);
      if (prestige == null) {
        sender.sendMessage(ChatColor.RED + "No prestige exists with name: " + rankName);
        return false;
      }
    } else {
      sender.sendMessage(ChatColor.RED + "As console, please specify the first prestige in the track you wish to view: /prestiges <prestige name>");
      return false;
    }

    while (prestige.hasNext()) {
      RankElement<Prestige> next = prestige.getNext();
      if (prestige.getRank().equals(playerRank)) {
        plugin.getMessage(sender, Message.PRESTIGES_CURRENT, prestige.getRank(), next.getRank())
            .send(sender);
        message = Message.PRESTIGES_INCOMPLETE;
      } else {
        MessageBuilder builder = plugin
            .getMessage(sender, message, prestige.getRank(), next.getRank());
        builder.send(sender);
      }
      prestige = next;
    }

    plugin.sendHeaderFooter(sender, playerRank, Message.PRESTIGES_FOOTER);
    return true;
  }
}
