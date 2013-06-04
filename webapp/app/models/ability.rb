class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    case user.role
    when 'super'
      can   :manage, :all
    when 'admin'
      can   :upload, :file
    when 'client'
    end
  end
end