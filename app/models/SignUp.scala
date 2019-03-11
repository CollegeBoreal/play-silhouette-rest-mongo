package models

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel(description = "SignUp object")
case class SignUp(
  @ApiModelProperty(value = "e-mail address", required = true, example = "james.bond@test.com") email: String,
  @ApiModelProperty(value = "user full name", required = true, example = "James Bond") fullName: String,
  @ApiModelProperty(value = "password", required = true, example = "this!Password!Is!Very!Very!Strong!") password: String,
  @ApiModelProperty(value = "user Terms", required = false, example = "false") terms: Boolean
)
