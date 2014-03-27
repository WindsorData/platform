#unicorn.rb Starts here 

home_path = ENV['HOME']
app_path = "#{home_path}/windsor/current" 

worker_processes 10
working_directory app_path
# needs to be the correct directory

user 'ubuntu', 'ubuntu'

# This loads the application in the master process before forking 
# worker processes 
# Read more about it here: 
# http://unicorn.bogomips.org/Unicorn/Configurator.html 
preload_app true 
timeout 1000

# This is where we specify the socket. 
# We will point the upstream Nginx module to this socket later on 
#listen "/opt/redmine/redmine/tmp/sockets/unicorn.sock", :backlog => 64 #directory structure needs to be created.  
listen 3333
pid "#{app_path}/tmp/pids/unicorn.pid" # make sure this points to a valid directory.  

# Set the path of the log files inside the log folder of the testapp 
stderr_path "#{app_path}/log/unicorn.stderr.log" 
stdout_path "#{app_path}/log/unicorn.stdout.log"  

before_fork do |server, worker| 
  # This option works in together with preload_app true setting 
  # What is does is prevent the master process from holding 
  # the database connection 
  defined?(ActiveRecord::Base) and 
    ActiveRecord::Base.connection.disconnect! 

  old_pid = "#{server.config[:pid]}.oldbin"
  if File.exists?(old_pid) && server.pid != old_pid
    begin
      Process.kill("QUIT", File.read(old_pid).to_i)
    rescue Errno::ENOENT, Errno::ESRCH
      # someone else did our job for us
    end
  end
end  

after_fork do |server, worker| 
  # Here we are establishing the connection after forking worker 
  # processes 
  defined?(ActiveRecord::Base) and 
  ActiveRecord::Base.establish_connection 
end 

#unicorn.rb Ends here
