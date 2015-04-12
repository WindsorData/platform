Deployment commands:

```
 cd /vagrant/core/
 1458  /play/play clean compile stage
 1459  git fetch origin 
 1460  git rebase origin/master 
 1461  sudo /play/play clean compile stage
 1462  git status 
 1463  ps ax | grep scala
 1464  ps ax | grep play
 1465  kill 25580
 1466  sudo kill 25580
 1467  ps ax | grep play
 1468  sudo nohup target/start &
```
