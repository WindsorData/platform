# bundle exec rails g scaffold_controller users --no-test-framework
class UsersController < ApplicationController
  inherit_resources
end
