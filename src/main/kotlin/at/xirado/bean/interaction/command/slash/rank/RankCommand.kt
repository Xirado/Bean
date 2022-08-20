package at.xirado.bean.interaction.command.slash.rank

//class RankCommand(override val application: Application) : SlashCommand("rank", "Displays the rank-card of a user.") {
//    init {
//        option<User>(name = "user", description = "The User to display the rank-card from.")
//    }
//
//    override suspend fun baseCommand(event: SlashCommandInteractionEvent) {
//        val user = event.getOption<User>("user")?: event.user
//        val experience = retrieveTotalExperience(event.guild!!.idLong, user.idLong)
//        if (experience < 100)
//            return if (user == event.user)
//                event.replyDefaultLocalized("commands.rank.not_ranked_self", ephemeral = true).queue()
//            else
//                event.replyDefaultLocalized("commands.rank.not_ranked", ephemeral = true).queue()
//
//        event.deferReply().queue()
//
//        val rankCard = generateCard(user, event.guild!!)
//
//        event.hook.sendFile(rankCard, "card.png").queue()
//    }
//}