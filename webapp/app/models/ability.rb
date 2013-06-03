class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    case user.role
    when 'super'
      can     :manage, User
    when 'admin'
    when 'client'
      
    end
  end
end