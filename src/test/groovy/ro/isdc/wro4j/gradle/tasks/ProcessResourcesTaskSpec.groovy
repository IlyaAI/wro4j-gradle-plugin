package ro.isdc.wro4j.gradle.tasks

import org.gradle.api.Project;
import org.gradle.api.internal.file.collections.FileCollectionAdapter;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.testfixtures.ProjectBuilder;

import ro.isdc.wro4j.gradle.tasks.tools.TempDir;
import ro.isdc.wro4j.gradle.tasks.tools.WroModelFromClosure;
import spock.lang.Specification

class ProcessResourcesTaskSpec extends Specification {
    def "Test precompile task"() {
        given:
            Project project = ProjectBuilder.builder().build()

            def dir = TempDir.createNew("process-js")
            def jsSrcDir = new File(dir, '/js')
            jsSrcDir.mkdirs()
            new File(jsSrcDir, 'test.js').append('function() { return "test";}')
            def folderDir = new File(jsSrcDir, '/folder')
            folderDir.mkdirs()
            new File(folderDir, 'test.js').append('function() { return "test";}')
            
            def destDestDir = new File(dir, '/dest')
            destDestDir.mkdirs()

            ProcessResourcesTask task = project.task('processResources', type: ProcessResourcesTask)
            task.srcFiles = new SimpleFileCollection(jsSrcDir)
            task.destDir = destDestDir
            task.model = WroModelFromClosure.read {
                groups {
                    group1 {
                        js 'test.js'
                    }
                }
            }

        when:
            task.processResources()

        then:
            new File(destDestDir, 'group1.js').exists()
            new File(destDestDir.absolutePath + '/folder', 'test.js').exists()
    }
    
    
}
