package ch.euclidian.main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.euclidian.main.model.Player;
import ch.euclidian.main.model.Postulation;
import ch.euclidian.main.model.Team;
import ch.euclidian.main.util.LogHelper;
import ch.euclidian.main.util.Ressources;
import ch.euclidian.main.util.request.MessageBuilderRequest;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;


public class CommandManagement {

  private CommandManagement() {
  }

  //							Main Command Section
  //-------------------------------------------------------------------------

  public static String showCommand(String commande, User user) {
    return "Erreur dans le choix de l'affichage";
  }

  public static String deleteCommand(String commande) {
    if(commande.substring(0, 4).equalsIgnoreCase("team")) {
      return deleteTeamCommand(commande);
    } else if(commande.substring(0, 14).equalsIgnoreCase("playerFromTeam")) {
      return deletePlayerOfTeamCommand(commande.substring(14));
    } else if (commande.substring(0, 7).equalsIgnoreCase("reports")){
      return deleteReportsCommand();
    }else {
      return "Erreur dans le choix de la suppression";
    }
  }

  public static String clearCommand(String commande) {
    if(commande.substring(0, 3).equalsIgnoreCase("bot")) {
      return clearSelectedChannel(Main.getGuild().getTextChannelById(commande.substring(4)), true);
    }else if (commande.substring(0, 3).equalsIgnoreCase("all")){
      return clearSelectedChannel(Main.getGuild().getTextChannelById(commande.substring(4)), false);
    }else {
      return "Erreur dans le choix du type de suppression";
    }

  }

  //               Clear Command
  //-----------------------------------------------------------------------

  public static String clearSelectedChannel(TextChannel textChannel, boolean onlyBot) {

    List<Message> messages = textChannel.getIterableHistory().stream()
        .limit(1000)
        .filter(m-> m.getAuthor().equals(Main.getJda().getSelfUser()))
        .collect(Collectors.toList());

    if(onlyBot) {

      for(int i = 0; i < messages.size(); i++) {
        messages.get(i).delete().queue();
      }
      return "Mes messages ont bien été supprimé !";

    }else {
      return "On ne supprime pas tous les messages de tous le monde comme sa voyons, un peu de bon sens";
    }
  }

  //							 Show Command
  //-----------------------------------------------------------------------

  public static List<MessageEmbed> showPostulationsCommand(String commande) throws RiotApiException {
    ArrayList<MessageEmbed> listesPostulation = new ArrayList<MessageEmbed>();
    for(int i = 0; i < Main.getPostulationsList().size(); i++) {
      listesPostulation.add(MessageBuilderRequest.createShowPostulation(Main.getPostulationsList().get(i), i + 1));
    }
    LogHelper.logSender("Postulations affichées");

    return listesPostulation;
  }

  public static List<String> showReportsCommand(){
    ArrayList<String> listReport = new ArrayList<>();

    for(int i = 0; i < Main.getReportList().size(); i++) {
      listReport.add(Main.getReportList().get(i));
    }
    LogHelper.logSender("Reports envoyés");

    return listReport;
  }


  //							Delete Command
  //-------------------------------------------------------------------------

  public static String deleteTeamCommand(String commande) {
    Team team = Main.getTeamByName(commande.split(" ")[1]);

    for(int i = 0; i < team.getCategory().getChannels().size(); i++) {
      team.getCategory().getChannels().get(i).delete().queue();
    }

    team.getCategory().delete().queue();
    team.getRole().delete().queue();

    String name = team.getName();

    Main.getTeamList().remove(team);

    LogHelper.logSender("Equipe " + name + " supprimé");

    return "Equipe " + name + " supprimé !";
  }

  public static String deleteReportsCommand() {
    Main.setReportList(new ArrayList<String>());

    LogHelper.logSender("Reports Supprimé");

    return "Les reports ont bien été supprimé";
  }
  
  public static String deletePlayerOfTeamCommand(String command) {
    String[] split = command.split(" ");
    if(split.length == 2) {
      Player player = Main.getPlayersByDiscordId(split[1]);
      if(player != null) {
        for(Team team : Main.getTeamList()) {
          for(Player playerInTeam : team.getPlayers()) {
            if(player.getDiscordUser().getId().equals(playerInTeam.getDiscordUser().getId())) {
              team.getPlayers().remove(player);
              return "Joueur supprimé de la team";
            }
          }
        }
        return "Id dans aucune équipe";
        
      }else {
        return "Id discord incorrect";
      }
    }else {
      return "Vous devez mettre l'id discord du joueur à supprimer";
    }
  }

  //							Postulation Command
  //-------------------------------------------------------------------------

  public static String postulationCommand(String[] postulation, Member member) {
    String lolPseudo = "";
    try {
      lolPseudo = postulation[1].split(":")[1].replaceAll(" ", "");
    } catch (Exception e) {
      return "Erreur dans le format du Pseudo. (Format : \"Mon pseudo : *Pseudo*\")";
    }

    Summoner summoner;
    try {
      summoner = Ressources.getRiotApi().getSummonerByName(Platform.EUW, lolPseudo);
    } catch (IllegalArgumentException e) {
      return "Votre pseudo n'est pas valide. Merci de vérifier la typographie du pseudo (Note : Il doit obligatoirement être de la région EUW)";
    } catch (RiotApiException e) {
      e.printStackTrace();
      return "L'api à rencontré un problème";
    }

    String[] position;

    try {
      position = postulation[2].split(":")[1].split(",");
    }catch (Exception e) {
      return "Erreur dans le format des rôles. (Format : \"Les rôles que je peux jouer : *Role, Role, Role*\")";
    }

    ArrayList<Role> roles = new ArrayList<>();

    try {
      for(int i = 0; i < position.length; i++) {
        position[i] = position[i].replaceAll(" ", "");
        if(Main.getPositionRoleByName(position[i]) == null) {
          throw new NullPointerException();
        }else {
          roles.add(Main.getPositionRoleByName(position[i]));
        }
      }
    }catch (NullPointerException e) {
      return "Erreur dans la sélection des postes !";
    }

    String horaire = "";
    try {
      horaire = postulation[3].split(":")[1];
    }catch (Exception e) {
      return "Erreur dans le format de l'heure. (Format : \"Horaires : *VosHoraires*\")";
    }

    int index = Main.getPostulationIndexByMember(member);

    Postulation postulationObject = new Postulation(member, summoner, roles, horaire);

    ArrayList<Role> roleWithPostulant = new ArrayList<>();
    roleWithPostulant.addAll(roles);
    roleWithPostulant.add(Main.getPostulantRole());

    if(index > -1) {
      Main.getPostulationsList().remove(index);
      Main.getPostulationsList().add(postulationObject);
      Main.getController().modifyMemberRoles(member, roleWithPostulant).queue();

      LogHelper.logSender("Postulation de " + member.getUser().getName() + " modifié");

      return "Votre postulation a bien été modifié";
    }else {
      Main.getPostulationsList().add(postulationObject);
      Main.getController().addRolesToMember(member, roleWithPostulant).queue();

      LogHelper.logSender("Nouvelle postulation créé par " + member.getUser().getName());

      return "Merci d'avoir postulé ! Vous recevrez des informations concernant votre potentiel recrutement très bientôt !\n"
      + "Votre postulations (Vous pouvez la modifier en renvoyant une postulation) : \n \n"
      + postulationObject.toString();
    }
  }

  public static String postulationAcceptCommand(int accepted, User user) {
    Postulation postulation;
    try {
      postulation = Main.getPostulationsList().get(accepted);
    }catch (IndexOutOfBoundsException e) {
      return "Erreur dans la sélection de la postulation (index)";
    }

    PrivateChannel privateChannel = postulation.getMember().getUser().openPrivateChannel().complete();
    privateChannel.sendTyping().queue();
    privateChannel.sendMessage("Votre postulation à été accepté, vous recevrez très bientôt des informations concernant votre futur affiliation, "
        + "C'est " + user.getName() + " qui s'occupera de vous contacter.").queue();

    String result = "Vous avez accepter la postulation de " + postulation.getMember().getUser().getName() + ". "
        + "Il a été automatiquement enregistré en tant que joueur.";

    Player player = new Player(postulation.getMember().getUser().getName(), postulation.getMember().getUser(), postulation.getSummoner(), false);
    Main.getController().addRolesToMember(postulation.getMember(), Main.getRegisteredRole()).queue();

    for(int i = 0; i < Main.getPlayerList().size(); i++) {
      if(Main.getPlayerList().get(i).getDiscordUser().getName().equals(postulation.getMember().getUser().getName())) {
        Main.getPlayerList().remove(i);
      }
    }

    Main.getPlayerList().add(player);

    Main.getPostulationsList().remove(accepted);

    LogHelper.logSender("Postulation de " + postulation.getMember().getUser().getName() + " accepté par " + user.getName());

    return result;
  }
}
