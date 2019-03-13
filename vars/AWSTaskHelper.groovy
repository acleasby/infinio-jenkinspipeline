// Using JsonSlurperClassic to avoid unserializable exceptions using JsonSlurper/LazyMap
import groovy.json.JsonSlurperClassic

def findAllTasks(String json) {
    println(json)
    String sb = ""
    def jsonSlurper = new JsonSlurperClassic()
    def object = jsonSlurper.parseText(json)
    for (String task in object.taskArns) {
        String taskNumber = getTaskNumber(task)
        sb = sb + taskNumber + " "
    }
    return sb;
}


List<String> findTasksAssociatedWithTaskName(String json, String taskName) {
    String baseTaskName = taskName.contains(":") ? taskName.substring(0, taskName.indexOf(":")) : taskName
    List<String> list = []
    def jsonObect = new JsonSlurperClassic().parseText(json)
    for (def task : jsonObect.tasks) {
        if (task.taskDefinitionArn.contains(baseTaskName)) {
            String taskID = getTaskNumber(task.taskArn)
            if (!taskID.isEmpty()) {
                list.add(taskID)
            }
        }
    }
    return list
}

def getTaskNumber(String task) {
    String taskNumber = ""
    int taskStart = task.indexOf("task/") + 5
    if (taskStart >= 5) {
        taskNumber = task.substring(taskStart)
    }
    return taskNumber
}

def stopTasks(String cluster, Collection<String> taskArns) {
    for (String taskArn : taskArns) {
        sh(script: "aws ecs stop-task --cluster ${cluster} --task ${taskArn}")
    }
    if (taskArns.size() > 0) {
        sh("aws ecs wait tasks-stopped --cluster ${cluster} --tasks ${taskArns.join(' ')}")                     \

    }
}

List<String> getRunningTaskArns(String cluster) {
    String jsonOutput = sh(returnStdout: true, script: "aws ecs list-tasks --cluster ${cluster}")
    def jsonObect = new JsonSlurperClassic().parseText(jsonOutput)
    List<String> taskArns = []
    for (String taskArn : jsonObect.taskArns) {
        taskArns.add(taskArn)
    }
    echo "Got running tasks in cluster ${cluster}: " + taskArns
    return taskArns
}

List<String> getRunningTaskArnsByDefinitionArn(String cluster, Collection<String> taskDefinitionArns) {
    List<String> tasks = []
    List<String> runningTaskArns = getRunningTaskArns(cluster)
    if (runningTaskArns.size() > 0) {
        String runningTaskArnList = runningTaskArns.join(" ")
        String jsonOutput = sh(returnStdout: true, script: "aws ecs describe-tasks --cluster ${cluster} --task ${runningTaskArnList}")
        def jsonObject = new JsonSlurperClassic().parseText(jsonOutput)
        for (def task : jsonObject.tasks) {
            if (taskDefinitionArns.contains(task.taskDefinitionArn)) {
                tasks.add(task.taskArn)
            }
        }
    }
    return tasks
}

List<String> getFamilyTaskDefinitionArns(String familyPrefix) {
    String jsonOutput = sh(returnStdout: true, script: "aws ecs list-task-definitions --family-prefix ${familyPrefix}")
    def jsonObject = new JsonSlurperClassic().parseText(jsonOutput)
    List<String> taskDefinitionArns = []
    for (String taskArn : jsonObject.taskDefinitionArns) {
        taskDefinitionArns.add(taskArn)
    }
    return taskDefinitionArns
}

boolean isAtleastOneInstanceAvailableInCluster(String cluster) {
    String jsonOutput = sh(returnStdout: true, script: " aws ecs list-container-instances --cluster ${cluster}")
    def jsonObject = new JsonSlurperClassic().parseText(jsonOutput)
    return jsonObject.containerInstanceArns.size() > 0
}

def stopTasksInFamily(String cluster, String familyPrefix) {
    echo "Stopping tasks in family ${familyPrefix}..."
    List<String> taskDefinitionArns = getFamilyTaskDefinitionArns(familyPrefix)
    echo "Got task arns: " + taskDefinitionArns
    stopTasks(cluster, getRunningTaskArnsByDefinitionArn(cluster, taskDefinitionArns))
    echo "Tasks are stopped"
}

def startTask(String cluster, String taskDefinitionArn) {
    String jsonOutput = sh(returnStdout: true, script: "aws ecs run-task --cluster ${cluster} --task-definition ${taskDefinitionArn}")
    def jsonObject = new JsonSlurperClassic().parseText(jsonOutput)
    List<String> taskArns = []
    for (def task : jsonObject.tasks) {
        taskArns.add(task.taskArn)
    }
    String taskArnString = taskArns.join(" ")
    echo "Waiting for tasks ${taskArnString} to start..."
    int result = sh(returnStatus: true, script: "aws ecs wait tasks-running --cluster ${cluster} --tasks ${taskArnString}")
    if (result != 0) {
        error("Failed to start all tasks. Result code ${result}.  Not all tasks running.")
    } else {
        echo "Tasks started successfully"
    }
}

def getTaskPrivateIP(String cluster, String taskDefinitionArn) {
    List<String> runningArns = getRunningTaskArnsByDefinitionArn(cluster, [taskDefinitionArn])
    assert runningArns.size() == 1: "Expected 1 running task but got " + runningArns.size()
    def task = describeTask(cluster, runningArns.get(0))
    def containerInstanceArn = task.containerInstanceArn
    def containerInstance = describeContainerInstance(cluster, containerInstanceArn)
    def ec2InstanceId = containerInstance.ec2InstanceId
    def ec2Instance = describeEc2Instance(ec2InstanceId)
    return ec2Instance.PrivateIpAddress
}

def describeEc2Instance(String instanceId) {
    String jsonOutput = sh(returnStdout: true, script: "aws ec2 describe-instances --instance-ids ${instanceId}")
    def instances = new JsonSlurperClassic().parseText(jsonOutput)
    assert instances.Reservations.size() == 1 && instances.Reservations[0].Instances.size() == 1: "Can't parse: \n" + jsonOutput
    return instances.Reservations[0].Instances[0]
}

def describeContainerInstance(String cluster, String containerInstanceArn) {
    String jsonOutput = sh(returnStdout: true, script: "aws ecs describe-container-instances --container-instances ${containerInstanceArn} --cluster ${cluster}")
    def instances = new JsonSlurperClassic().parseText(jsonOutput)
    assert instances.containerInstances.size() == 1: "Expected 1 container instance but got " + instances.containerInstances.size()
    return instances.containerInstances[0]
}

def describeTask(String cluster, String runningTaskArn) {
    String jsonOutput = sh(returnStdout: true, script: "aws ecs describe-tasks --cluster ${cluster} --task ${runningTaskArn}")
    def tasks = new JsonSlurperClassic().parseText(jsonOutput)
    assert tasks.tasks.size() == 1: "Expected 1 task result but got " + tasks.tasks.size() + " from:\n${jsonOutput}"
    return tasks.tasks[0]
}

return this