package ch.euclidian.main.model.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
import ch.euclidian.main.model.Player;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.Member;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class RegisterPlayerCommand extends Command {
  
  private static final Logger logger = LoggerFactory.getLogger(RegisterPlayerCommand.class);

  public RegisterPlayerCommand() {
    this.name = "register";
    this.arguments = "VotrePseudoLoL";
    this.help = "Permet de vous enregistrer en tant que joueur";
  }
  
  @Override
  protected void execute(CommandEvent event) {
    String pseudo = null;

    try {
      pseudo = event.getArgs();
      
      for(int i = 0; i < Main.getPlayerList().size(); i++) {
        if(Main.getPlayerList().get(i).getSummoner().getName().equals(pseudo)) {
          event.reply("Ce compte est déjà enregistré");
        }
      }

    }catch(ArrayIndexOutOfBoundsException e) {
      event.reply("Erreur dans l'enregistrement. Note : Vous devez écrire \"register player (VotrePseudo)\" pour vous enregistrer");
    }


    Member member = event.getMember();

    for(int i = 0; i < member.getRoles().size(); i++) {
      if(member.getRoles().get(i).equals(Main.getRegisteredRole())) {
        event.reply("Vous êtes déjà enregistée !");
      }
    }

    Summoner summoner = null;
    try {
      summoner = Ressources.getRiotApi().getSummonerByName(Platform.EUW, pseudo);
    } catch (RiotApiException e) {
      logger.error(e.getMessage());

      event.reply("Un problème avec l'api est survenu");
    } catch (IllegalArgumentException e) {
      event.reply("Aucun compte à ce nom. Vérfier le pseudo écrit");
    }

    Player player = new Player(event.getAuthor().getName(), event.getAuthor(), summoner, true);

    Main.getPlayerList().add(player);

    Main.getController().addRolesToMember(member, Main.getRegisteredRole()).queue();

    LogHelper.logSender(event.getAuthor().getName() + " c'est enregistré en tant que joueur");

  }

}
