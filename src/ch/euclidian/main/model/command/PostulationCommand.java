package ch.euclidian.main.model.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import ch.euclidian.main.Main;
import ch.euclidian.main.util.Ressources;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class PostulationCommand extends Command{

  private final EventWaiter waiter;
  private Summoner summoner;
  
  public PostulationCommand(EventWaiter waiter) {
    this.waiter = waiter;
    this.name = "postulation";
    this.aliases = new String[]{"post"};
    this.help = "Lance une procédure de postulation";
  }

  @Override
  protected void execute(CommandEvent event) {

    event.reply("Bien ! Tous d'abord, donnez moi votre pseudo LoL");
    
    waiter.waitForEvent(MessageReceivedEvent.class,
        e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()),
        e -> receivePseudoAndContinue(event, e),
        3, TimeUnit.MINUTES, () -> event.reply("Vous avez pris trop de temps pour répondre"));
  }
  
  private void receivePseudoAndContinue(CommandEvent commandEvent, MessageReceivedEvent messageEvent){
    String pseudo = messageEvent.getMessage().getContentRaw();
    
    try {
      summoner = Ressources.getRiotApi().getSummonerByName(Platform.EUW, pseudo);
    } catch (IllegalArgumentException e) {
      commandEvent.reply("Votre pseudo n'est pas valide. Merci de vérifier la typographie du pseudo et de recommencer la procédure une fois cela fait"
          + " (Note : Il doit obligatoirement être de la région EUW)");
      return;
    } catch (RiotApiException e) {
      commandEvent.replyError("L'api à rencontré un problème. Merci de retenter plus tard");
      return;
    }
    
    commandEvent.reply("Très bien, dites nous maintenant quel rôle vous souhaitez jouer en répondant exactement soit : top, jng, mid, adc, sup");
    
    waiter.waitForEvent(MessageReceivedEvent.class,
        e -> e.getAuthor().equals(commandEvent.getAuthor()) && e.getChannel().equals(commandEvent.getChannel()),
        e -> receiveRoleAndContinue(commandEvent, e, new ArrayList<>()),
        3, TimeUnit.MINUTES, () -> commandEvent.reply("Vous avez pris trop de temps pour répondre"));
  }
  
  private void receiveRoleAndContinue(CommandEvent commandEvent, MessageReceivedEvent messageEvent, List<Role> listeRole) {
    String roleStr = messageEvent.getMessage().getContentRaw();
    
    
    if(roleStr.equalsIgnoreCase("top")) {
    }
  }

}
