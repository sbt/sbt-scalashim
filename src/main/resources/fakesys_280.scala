/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

$package$

object sys {
  import scala.collection.immutable
  import scala.collection.{ mutable, Iterator }
  import java.security.AccessControlException  

  /** Throw a new RuntimeException with the supplied message.
   *
   *  @return   Nothing.
   */  
  def error(message: String): Nothing = throw new RuntimeException(message)

  /** Exit the JVM with the default status code.
   *
   *  @return   Nothing.
   */
  def exit(): Nothing = exit(0)

  /** Exit the JVM with the given status code.
   *
   *  @return   Nothing.
   */    
  def exit(status: Int): Nothing = {
    java.lang.System.exit(status)
    throw new Throwable()
  }

  /** A convenience method to get the current Runtime instance.
   *
   *  @return   the result of `java.lang.Runtime.getRuntime()`
   */
  def runtime: Runtime = Runtime.getRuntime

  /** A bidirectional, mutable Map representing the current system Properties.
   *
   *  @return   a SystemProperties.
   *  @see      `scala.sys.SystemProperties`
   */
  def props: SystemProperties = new SystemProperties

  /** An immutable Map representing the current system environment.
   *
   *  @return   a Map containing the system environment variables.
   */
  def env: immutable.Map[String, String] = {
    import scala.collection.JavaConversions._
    // immutable.Map(System.getenv().asScala.toSeq: _*)
    immutable.Map(System.getenv().toSeq: _*)
  }

  /** Returns all active thread in the current thread's thread group and subgroups.
   *
   *  @return   an IndexedSeq containing the threads.
   */
  def allThreads(): IndexedSeq[Thread] = {
    val num    = Thread.activeCount()
    val tarray = new Array[Thread](num)
    val got    = Thread.enumerate(tarray)

    tarray take got
  }

  /** A bidirectional map wrapping the java System properties.
   *  Changes to System properties will be immediately visible in the map,
   *  and modifications made to the map will be immediately applied to the
   *  System properties.  If a security manager is in place which prevents
   *  the properties from being read or written, the AccessControlException
   *  will be caught and discarded.
   *
   *  @author Paul Phillips
   *  @version 2.9
   *  @since   2.9
   */
  class SystemProperties
  extends mutable.Map[String, String] {

    override def empty = new SystemProperties
    override def default(key: String): String = null

    def iterator: Iterator[(String, String)] = {
      import scala.collection.JavaConversions._
      // wrapAccess(System.getProperties().asScala.iterator) getOrElse Iterator.empty
      wrapAccess(System.getProperties().iterator) getOrElse Iterator.empty
    }
      
    def get(key: String) =
      wrapAccess(Option(System.getProperty(key))) flatMap (x => x)
    override def contains(key: String) =
      wrapAccess(super.contains(key)) exists (x => x)

    def -= (key: String): this.type = { wrapAccess(System.clearProperty(key)) ; this }
    def += (kv: (String, String)): this.type = { wrapAccess(System.setProperty(kv._1, kv._2)) ; this }

    def wrapAccess[T](body: => T): Option[T] =
      try Some(body) catch { case _: AccessControlException => None }
  }

  /** The values in SystemProperties can be used to access and manipulate
   *  designated system properties.  See `scala.sys.Prop` for particulars.
   *  @example {{{
   *    if (!headless.isSet) headless.enable()
   *  }}}
   */
  object SystemProperties {
    import Prop._

    /** An unenforceable, advisory only place to do some synchronization when
     *  mutating system properties.
     */
    def exclusively[T](body: => T) = this synchronized body

    implicit def systemPropertiesToCompanion(p: SystemProperties): SystemProperties.type = this
    private lazy val propertyHelp = mutable.Map[String, String]()
    private def addHelp[P <: Prop[_]](p: P, helpText: String): P = {
      propertyHelp(p.key) = helpText
      p
    }
    private def str(key: String, helpText: String) = addHelp(Prop[String](key), helpText)
    private def bool(key: String, helpText: String): BooleanProp = addHelp[BooleanProp](
      if (key startsWith "java.") BooleanProp.valueIsTrue(key) else BooleanProp.keyExists(key),
      helpText
    )
    def help(key: String) = propertyHelp.getOrElse(key, "")

    // Todo: bring some sanity to the intersection of system properties aka "mutable
    // state shared by everyone and everything" and the reality that there is no other
    // mechanism for accomplishing some things on the jvm.
    lazy val headless            = bool("java.awt.headless", "system should not utilize a display device")
    lazy val preferIPv4Stack     = bool("java.net.preferIPv4Stack", "system should prefer IPv4 sockets")
    lazy val preferIPv6Addresses = bool("java.net.preferIPv6Addresses", "system should prefer IPv6 addresses")
    lazy val noTraceSupression   = bool("scala.control.noTraceSuppression", "scala should not suppress any stack trace creation")
    lazy val traceSourcePath     = str("scala.control.sourcepath", "sourcepath for looking up stack trace elements")
  }

  /** A lightweight interface wrapping a property contained in some
   *  unspecified map.  Generally it'll be the system properties but this
   *  is not a requirement.
   *
   *  See `scala.sys.SystemProperties` for an example usage.
   *
   *  @author Paul Phillips
   *  @version 2.9
   *  @since   2.9
   */
  trait Prop[+T] {
    /** The full name of the property, e.g. "java.awt.headless".
     */
    def key: String

    /** If the key exists in the properties map, converts the value
     *  to type `T` using valueFn.  As yet no validation is performed:
     *  it will throw an exception on a failed conversion.
     *  @return   the converted value, or `zero` if not in the map
     */
    def value: T

    /** True if the key exists in the properties map.  Note that this
     *  is not sufficient for a Boolean property to be considered true.
     *  @return   whether the map contains the key
     */
    def isSet: Boolean

    /** Sets the property.
     *
     *  @param    the new string value
     *  @return   the old value, or null if it was unset.
     */
    def set(newValue: String): String

    /** Sets the property with a value of the represented type.
     */
    def setValue[T1 >: T](value: T1): T

    /** Gets the current string value if any.  Will not return null: use
     *  `isSet` to test for existence.
     *  @return   the current string value if any, else the empty string
     */
    def get: String

    /** Some(value) if the property is set, None otherwise.
     */
    def option: Option[T]

    /** Removes the property from the underlying map.
     */
    def clear(): Unit

    /** A value of type `T` for use when the property is unset.
     *  The default implementation delivers null for reference types
     *  and 0/0.0/false for non-reference types.
     */
    protected def zero: T
  }

  object Prop {
    /** A creator of property instances.  For any type `T`, if an implicit
     *  parameter of type Creator[T] is in scope, a Prop[T] can be created
     *  via this object's apply method.
     */
    // @annotation.implicitNotFound("No implicit property creator available for type ${T}.")
    trait Creator[+T] {
      /** Creates a Prop[T] of this type based on the given key. */
      def apply(key: String): Prop[T]
    }

    implicit val FileProp: Prop.Creator[java.io.File] = new CreatorImpl[java.io.File](s => new java.io.File(s)) {}
    implicit val StringProp: Prop.Creator[String] = new CreatorImpl[String](s => s) {}
    implicit val IntProp: Prop.Creator[Int] = new CreatorImpl[Int](_.toInt) {}
    implicit val DoubleProp: Prop.Creator[Double] = new CreatorImpl[Double](_.toDouble) {}

    def apply[T: Creator](key: String): Prop[T] = implicitly[Creator[T]] apply key
  }

  /** A few additional conveniences for Boolean properties.
   */
  trait BooleanProp extends Prop[Boolean] {
    /** The semantics of value are determined at Prop creation.  See methods
     *  `valueIsTrue` and `keyExists` in object BooleanProp for examples.
     *
     *  @return   true if the current String is considered true, false otherwise
     */
    def value: Boolean

    /** Alter this property so that `value` will be true. */
    def enable(): Unit

    /** Alter this property so that `value` will be false. */
    def disable(): Unit

    /** Toggle the property between enabled and disabled states. */
    def toggle(): Unit
  }

  object BooleanProp {
    private[this]
    class BooleanPropImpl(key: String, valueFn: String => Boolean) extends PropImpl(key, valueFn) with BooleanProp {
      override def setValue[T1 >: Boolean](newValue: T1): Boolean = newValue match {
        case x: Boolean if !x   => val old = value ; clear() ; old
        case x                  => super.setValue(newValue)
      }
      def enable()  = this setValue true
      def disable() = this.clear()
      def toggle()  = if (value) disable() else enable()
    }
    private[this]
    class ConstantImpl(val key: String, val value: Boolean) extends BooleanProp {
      val isSet = value
      def set(newValue: String) = "" + value
      def setValue[T1 >: Boolean](newValue: T1): Boolean = value
      def get: String = "" + value
      val clear, enable, disable, toggle = ()
      def option = if (isSet) Some(value) else None

      protected def zero = false
    }

    /** The java definition of property truth is that the key be in the map and
     *  the value be equal to the String "true", case insensitively.  This method
     *  creates a BooleanProp instance which adheres to that definition.
     *
     *  @return   A BooleanProp which acts like java's Boolean.getBoolean
     */
    def valueIsTrue[T](key: String): BooleanProp = new BooleanPropImpl(key, _.toLowerCase == "true")

    /** As an alternative, this method creates a BooleanProp which is true
     *  if the key exists in the map.  This way -Dfoo.bar is enough to be
     *  considered true.
     *
     *  @return   A BooleanProp with a liberal truth policy
     */
    def keyExists[T](key: String): BooleanProp = new BooleanPropImpl(key, _ => true)

    /** A constant true or false property which ignores all method calls.
     */
    def constant(key: String, isOn: Boolean): BooleanProp = new ConstantImpl(key, isOn)

    implicit def booleanPropAsBoolean(b: BooleanProp): Boolean = b.value
  }

  /** The internal implementation of Prop.
   */
  private[this] class PropImpl[+T](val key: String, valueFn: String => T) extends Prop[T] {
    def value: T = if (isSet) valueFn(get) else zero
    def isSet    = underlying contains key
    def set(newValue: String): String = {
      val old = if (isSet) get else null
      underlying(key) = newValue
      old
    }
    def setValue[T1 >: T](newValue: T1): T = {
      val old = value
      if (newValue == null) set(null)
      else set("" + newValue)
      old
    }
    def get: String =
      if (isSet) underlying.getOrElse(key, "")
      else ""

    def clear(): Unit = underlying -= key
    def option: Option[T] = if (isSet) Some(value) else None
    def or[T1 >: T](alt: => T1): T1 = if (isSet) value else alt

    /** The underlying property map, in our case always sys.props */
    protected def underlying: mutable.Map[String, String] = sys.props
    protected def zero: T = null.asInstanceOf[T]
    private def getString = if (isSet) "currently: " + get else "unset"
    override def toString = "%s (%s)".format(key, getString)
  }

  private[this] abstract class CreatorImpl[+T](f: String => T) extends Prop.Creator[T] {
    def apply(key: String): Prop[T] = new PropImpl[T](key, f)
  }


}
