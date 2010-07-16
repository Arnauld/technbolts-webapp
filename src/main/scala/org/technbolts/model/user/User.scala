package org.technbolts.model.user

// Import all of the mapper classes
import _root_.net.liftweb.mapper._
import net.liftweb.common.Full

// Create a User class extending the Mapper base class
// MegaProtoUser, which provides default fields and methods
// for a site user.
class User extends MegaProtoUser[User] {
  def getSingleton = User // reference to the companion object below

  // define an additional field for a personal essay
  object textArea extends MappedTextarea(this, 2048) {
    override def textareaRows  = 10
    override def textareaCols = 50
    override def displayName = "Personal Essay"
  }
}

// Create a "companion object" to the User class (above).
// The companion object is a "singleton" object that shares the same
// name as its companion class. It provides global (i.e. non-instance)
// methods and fields, such as find, dbTableName, dbIndexes, etc.
// For more, see the Scala documentation on singleton objects
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name

  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email, locale, timezone, password, textArea)

  // comment this line out to require email validations
  override def skipEmailValidation = true

  // Spruce up the forms a bit
  override def screenWrap = Full(
      <lift:surround with="default" at="content">
          <div id="formBox">
              <lift:bind />
          </div>
      </lift:surround>)

  // Provide our own login page template.
  /* overall wrapping in screenWrap

  override def loginXhtml =
    <lift:surround with="default" at="content">
      {super.loginXhtml}
    </lift:surround>
    */

  // Provide our own signup page template.
  /* overall wrapping in screenWrap
  
  override def signupXhtml(user: User) =
    <lift:surround with="default" at="content">
      {super.signupXhtml(user)}
    </lift:surround>
    */
}