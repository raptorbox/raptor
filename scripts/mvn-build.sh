mvn install -DskipTests=true

cd raptor-broker
mvn package -DskipTests=true
cd ..

cd raptor-api

cd raptor-action
mvn package -DskipTests=true
cd ..

cd raptor-auth
mvn package -DskipTests=true
cd ..

cd raptor-inventory
mvn package -DskipTests=true
cd ..

cd raptor-profile
mvn package -DskipTests=true
cd ..

cd raptor-stream
mvn package -DskipTests=true
cd ..

cd raptor-tree
mvn package -DskipTests=true
cd ..

cd ..
