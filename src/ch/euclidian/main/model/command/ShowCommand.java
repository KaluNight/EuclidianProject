package ch.euclidian.main.model.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.euclidian.main.Main;
import ch.euclidian.main.util.request.MessageBuilderRequest;

public class ShowCommand extends Command{

  public ShowCommand() {
    this.name = "show";
    this.arguments = "ThingToShow";
    this.help = "Permet d'afficher une liste d'élément selon le paramètre";
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent event) {
    boolean oneMessageAsBeenSended = false;
    
    event.getChannel().sendTyping().complete();
    if(event.getArgs().equalsIgnoreCase("postulation")) {
      for(int i = 0; i < Main.getPostulationsList().size(); i++) {
        event.reply(MessageBuilderRequest.createShowPostulation(Main.getPostulationsList().get(i), i + 1));
        oneMessageAsBeenSended = true;
      }
      if(!oneMessageAsBeenSended) {
        event.reply("Aucune postulation existante");
      }
    }else if(event.getArgs().equalsIgnoreCase("report")) {
      for(String report : Main.getReportList()) {
        event.reply(report);
        oneMessageAsBeenSended = true;
      }
      if(!oneMessageAsBeenSended) {
        event.reply("Aucune report existant");
      }
    }else {
      event.reply("Impossible d'afficher \"" + event.getArgs() + "\" !");
    }
  }

}
