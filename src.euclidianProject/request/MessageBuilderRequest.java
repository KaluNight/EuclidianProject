package request;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class MessageBuilderRequest {
	
	public static MessageEmbed createInfo() {
		
		EmbedBuilder message = new EmbedBuilder();

		return message.build();
	}
}
