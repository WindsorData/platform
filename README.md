Deployment commands:

```
 $ cd /vagrant/core/
 $  git fetch origin 
 $  git rebase origin/master 
 $  sudo /play/play clean compile stage
 $  sudo pkill -of NettyServer
 $  sudo nohup target/start &
```
