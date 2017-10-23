import java.io.*
import java.util.*

/**
 * Gets the current build properties from build.properties and returns a Properties object
 * @return
 */
def call() {
    def buildPropertiesContents = readFile file: "portal/ui/target/classes/build.properties"
    Properties props = new Properties()
    props.load(new StringReader(buildPropertiesContents))

    return props
}
