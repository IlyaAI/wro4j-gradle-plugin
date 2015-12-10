package ro.isdc.wro4j.gradle

class WroSpec {
    private final Set<String> targetGroups
    private final List<String> preProcessors = []
    private final List<String> postProcessors = []

    WroSpec(String group) {
        targetGroups = [group]
    }

    String getName() {
        return targetGroups.first()
    }

    Set<String> getTargetGroups() {
        return targetGroups
    }

    void targetGroup(String...groups) {
        targetGroups.addAll(groups)
    }

    List<String> getPreProcessors() {
        return preProcessors
    }

    void preProcessor(String...pre) {
        preProcessors.addAll(pre)
    }

    List<String> getPostProcessors() {
        return postProcessors
    }

    void postProcessor(String...post) {
        postProcessors.addAll(post)
    }
}
