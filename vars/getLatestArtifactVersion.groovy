import com.infinio.Version
@com.cloudbees.groovy.cps.NonCPS
@Grab(group = 'net.sourceforge.nekohtml', module = 'nekohtml', version = '1.9.14')
import org.cyberneko.html.parsers.SAXParser
@com.cloudbees.groovy.cps.NonCPS
@Grab(group = 'net.sourceforge.nekohtml', module = 'nekohtml', version = '1.9.14')
import org.cyberneko.html.parsers.SAXParser

/**
 * Resolves an artifact from Nexus and returns metadata about it.  Returns null if the artifact was not found.
 * @param args
 * @return
 */

def call(Map args) {
    assert args.containsKey("repository"): "repository must be provided"
    assert args.containsKey("groupId"): "groupId must be provided"
    assert args.containsKey("artifactId"): "artifactId must be provided"

    List<Version> versions = new ArrayList<>()
    String artifactPath = args.groupId.replace(".", "/") + "/" + args.artifactId
    String artifactUrl = "https://nexus.infinio.com/content/repositories/${args.repository}/${artifactPath}"
    URL metadataXmlURL = new URL(artifactUrl + "/maven-metadata.xml")
    try {
        def metadata = new XmlSlurper().parseText(metadataXmlURL.text)
        versions.addAll(metadata.versioning.versions.children().collect { version ->
            return new Version(version.toString())
        })
    } catch (FileNotFoundException e) {
        try {
            versions.addAll(getVersions(new URL(artifactUrl)).collect { version ->
                return new Version(version.toString())
            })
        } catch (FileNotFoundException ex) {
            echo "Did not find artifact at: ${artifactUrl.toString()}"
            return null
        }
    }
    Collections.sort(versions)
    Collections.reverse(versions)
    echo "Found available versions: " + versions.toString()
    return versions.isEmpty() ? null : versions.get(0)
}

Collection<String> getVersions(URL repositoryUrl) {
    def parser = new SAXParser()
    def page = new XmlSlurper(parser).parseText(repositoryUrl.getText(requestProperties: ['User-Agent': 'Non empty']))

    return page.depthFirst().findAll { it.name() == "A" && !it.text().contains("Parent") }.collect { it.text().replace("/", "") }
}