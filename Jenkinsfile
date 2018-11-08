node ('beaware-jenkins-slave') {
    stage('Download Latest') {
        git(url: 'https://github.com/beaware-project/social-media-clustering-live.git', branch: 'master')
        sh 'git submodule init'
        sh 'git submodule update'
    }

    stage ('Compile (Maven)') {
        sh 'mvn clean package'
    }

    stage ('Build docker image') {
	    sh 'docker build -t beaware/social-media-clustering-live:${BUILD_NUMBER} .'
    }

    stage ('Push docker image') {
        withDockerRegistry([credentialsId: 'dockerhub-credentials']) {
		sh 'docker push beaware/social-media-clustering-live:${BUILD_NUMBER}'
        }
    }

    stage ('Deploy') {
	sh ''' 
		sed -i 's/IMAGE_TAG/'"$BUILD_NUMBER"'/g' kubernetes/deploy.yaml '''
        sh 'kubectl apply -f kubernetes/deploy.yaml -n prod --validate=false'
    }

    stage ('Print-deploy logs') {
        sh 'sleep 60'
        sh 'kubectl  -n prod logs deploy/social-media-clustering-live -c social-media-clustering-live'
    }
}
