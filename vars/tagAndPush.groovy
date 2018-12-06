

def call(Map args) {
    assert args.containsKey("tag"): "tag must be provided"
    String tag = args.get("tag")
    String message = args.get("message")

    sh("""
git tag -af ${tag} -m '${message}'
git push -f --tags
""")

}