class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    case user.role
    when 'super'
      can   :manage, User
    when 'admin'
      can   :upload, :file
    when 'client'
      can   :create, Group
    end
  end
end