import com.cloudbees.groovy.cps.NonCPS

import java.nio.file.Path
import java.nio.file.Paths

def call(Map args) {
    assert args.containsKey("dir"): "dir must be provided"
    String dir = args.get("dir")
    boolean ignoreExisting = args.get("ignoreExisting", true)

    mkdir(dir, ignoreExisting)
}

@NonCPS
def mkdir(String dir, boolean ignoreExisting = true) {
    Path p = Paths.get(".", dir)
    echo "Full path: ${p.toAbsolutePath()}"
    File dirFile = new File(dir)
    if (!dirFile.exists()) {
        if (!dirFile.mkdirs()) {
            throw new IllegalStateException("Could not create directory ${dir}")
        }
    } else if (!ignoreExisting) {
        throw new IllegalArgumentException("Directory ${dir} already exists")
    }
}