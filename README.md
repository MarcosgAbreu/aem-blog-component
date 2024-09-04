COMO INSTALAR O COMPONENTE

	1. Usando maven crie um projeto aem
	mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate ^
	    -D archetypeGroupId=com.adobe.aem ^
	    -D archetypeArtifactId=aem-project-archetype ^
	    -D archetypeVersion=50 ^
	    -D appTitle="WKND Sites Project" ^
	    -D appId="wknd" ^
	    -D groupId="com.adobe.aem.guides" ^
	    -D artifactId="aem-guides-wknd" ^
	    -D package="com.adobe.aem.guides.wknd" ^
	    -D version="0.0.1-SNAPSHOT" ^
	    -D aemVersion="cloud"
	
	2. Altere a versão do SDK para 2024.7.17098.20240711T134106Z-240600 no pom.xml
	
	3. Coloque os arquivos na pasta core em suas respectivas pastas
	
	4. Faça o build da pasta core com o comando: 
		mvn clean install -PautoInstallSinglePackage

Teste as requisições HTTP via postman na URL: localhost:4502/bin/blog

![image](https://github.com/user-attachments/assets/82c146fe-c737-4d64-be8e-f9de4eb7e376)

