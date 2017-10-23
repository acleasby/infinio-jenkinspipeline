/**
 * Pipeline extension to run a bash script on a windows slave.  Writes script to a temporary file, then executes it using cygwin bash.
 *
 * @param args
 * @return
 */
def call(Map args) {

    String bashExe = args.get("baseExePath", "C:\\cygwin64\\bin\\bash.exe")
    String scriptFilename = args.get("scriptFilename", "tmp-" + System.currentTimeMillis() + ".sh")
    String script = args.("script")

    writeFile file: scriptFilename, text: "#!$bashExe\n$script"

    bat "$bashExe $scriptFilename"

}
