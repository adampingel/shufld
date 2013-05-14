package controllers

import play.api.data.Form
import play.api.data.Forms._

object Forms {

  val createGameForm = Form(
    tuple(
      "which" -> nonEmptyText,
      "label" -> nonEmptyText
    )
  )

  val moveForm = Form(
    tuple(
      "which" -> nonEmptyText,
      "amount" -> optional(number)
    )
  )
  
  val loginForm = Form(single(
    "openid" -> nonEmptyText
  ))

}
