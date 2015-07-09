kura_addons
===========

Eclipse Kura add-ons that don't belong in the Eclipse Kura repository


How to install an artifact
--------------------------

For a specific jar:

  - Checkout the mvn-repo branch `git checkout mvn-repo` and pull the latest changes

  - From the kura_addons root directory, run the following (substituting the appropriate values):
```mvn deploy:deploy-file -DgroupId=org.eclipse.kura -DartifactId=org.eclipse.kura.web -Dversion=1.0.0 -Dpackaging=jar -Dfile=/tmp/org.eclipse.kura.web_1.0.0.jar -Durl=file://. ```

  - Commit the files that were added, then push to github
