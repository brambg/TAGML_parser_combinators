import lambdada.parsec.parser.* // combinators, e.g. string, char, not, ...
import lambdada.parsec.parser.Response.* // for reading the parser result (Accept, Reject)
import lambdada.parsec.io.Reader // for running parsers (Reader)


/* trying to build a TAGML parser using parser combinators in Kotlin
  13-10-2019

  Ronald Haentjens Dekker
 */

//TODO: rename!
data class MCTNode(val name: String, val listOfOpenNodes: List<OpenMCTNode>, val listOfClosedNodes: List<ClosedMCTNode>)
data class OpenMCTNode(val name: String, val open: Boolean = true)
data class ClosedMCTNode(val name: String, val open: Boolean = false)

// basic TAGML parsers
// NOTE: CharRange A - z INCLUDES [ and ] !!!!!
val opentagParser: Parser<Char, OpenMCTNode> = char('[').thenRight(charIn(CharRange('a', 'z')).rep).thenLeft(char('>')).map { OpenMCTNode (String(it.toCharArray())) }
val closeTagParser: Parser<Char, ClosedMCTNode> = char('<').thenRight(charIn(CharRange('a', 'z')).rep).thenLeft(char(']')).map { ClosedMCTNode(String(it.toCharArray())) }

fun main() {


    // We want to return the root node, which has a child node
    val moreComplexTAGML = Reader.string("[root>[child><child]<root]")




//    val myparser: Parser<Char, Pair<Pair<Pair<OpenMCTNode, OpenMCTNode>, ClosedMCTNode>, ClosedMCTNode>> = opentagParser.then(opentagParser).then(closeTagParser).then(closeTagParser)


    // once we get a close tag we need to reduce a


    // the way we does this is we first look for an open tag!
    // Then we look for a close tag...
    // When we encounter a close tag we go over the list of things we have seen thus far.

    // how do I create a new parser where I can state myself whether it is a success or failure?

    val response: Response<Char, MCTNode> = parseTAGML(moreComplexTAGML)
    println(response)
}

fun parseTAGML(reader: Reader<Char>): Response<Char, MCTNode> {
    println(reader)
    val temp_list_of_open_tags =  (opentagParser.rep)(reader)
    println(temp_list_of_open_tags)
    //temp_list_of_open_tags.fold()
    if (temp_list_of_open_tags is Accept) {
        // look for a close tag using the same reader as the previous parser (.input)
        val close_tag = closeTagParser(temp_list_of_open_tags.input)
        println(close_tag)
        // check whether the close tag is in the open tags...
        // if not throw an error (reject)
        // if yes... remove from open tags
        // so being functional means that we do not change the state of the input variables
        // we return continuously a new object
        val list_open_of_nodes = temp_list_of_open_tags.value
        if (close_tag is Accept) {
            val close_tag_node = close_tag.value
            println(list_open_of_nodes)
            println(close_tag_node)
            // we look for the name of the close tag in the open tags...
            // if it is not there it is an error...
            // otherwise we create an MCTNode object (maybe this should become MCT graph?)
            val openMCTNode: OpenMCTNode = list_open_of_nodes.last { node -> node.name == close_tag_node.name }
            // we must create a new MCTNode object
            // the one thing about this I don't like is that we will create lots of objects all the time
            // should this list be a copy? No, because it can't be changed; garbage collectionn is going to have a field day.
            val listOfClosedNodes = listOf(close_tag_node)
            //TODO: still a node too many in open
            println("here!")
            //returns<Char, MCTNode>()
            return Accept<Char, MCTNode>(
                MCTNode("blabla", list_open_of_nodes, listOfClosedNodes),
                close_tag.input,
                true
            )
        }
    }
    //TODO: This response is hard coded, and needs to change
    return Reject(reader.location(), false)
}


fun test() {
    val foo: Parser<Char, List<Char>> = not(char(',')).rep
    val input = Reader.string("hello, parsec!")
    val foobar = foo(input)
    println(foobar)

    when (foobar) {
        is Accept -> println("good")
        is Reject -> println("bad")
    }
    // good

    val tagml = Reader.string("[tagml>")
    val tagmlParser: Parser<Char, MCTNode> = string("[tagml>").map { MCTNode("tagml", emptyList(), emptyList()) }
    val result = tagmlParser(tagml)
    println(result)

    val identifierTest = Reader.string("test122343")
    val identifier: Parser<Char, List<Char>> = (charIn(CharRange('0', 'z'))).rep
    val result2 = identifier(identifierTest)
    println(result2)

    println(CharRange('A', 'z').asIterable().toList())

    // close tag test
    val testCloseTag = Reader.string("<child]")
    val mytestparser: Parser<Char, ClosedMCTNode> = closeTagParser
    val resultt = mytestparser(testCloseTag)
    println(resultt)


}