/**
 * Modifies a properties file using provided callbacks
 * @param args
 */
def call(Map args) {
    assert args.containsKey("file"): "file must be provided"
    assert args.containsKey("editFunction"): "editFunction must be provided"

    String contents = readFile(file: args.file)
    StringBuilder sb = new StringBuilder()
    for (String line : contents.split("\\r?\\n")) {
        echo "Processing line: " + line
        sb.append(args.editFunction.call(line) + "\n")
    }

    writeFile(file: args.containsKey("outputFile") ? args.outputFile : args.file, text: sb.toString())
}