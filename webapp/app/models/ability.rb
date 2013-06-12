class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    case user.role
    when 'super'
      can   :manage, User
      can   [:read, :create], Group
      can   :upload, :file
      can   :read_multiple, Array do |arr|
        arr.inject(true){|r, el| r && can?(:read, el)}
      end
      can   :perform, :quick_search
    when 'admin'
      can   :upload, :file
    when 'client'
      can   :create, Group
      can   :destroy, Group do |g|
        g.user == user
      end
      can   :read, Group do |g|
        g.company == user.company && user.company
      end
      can   :read_multiple, Array do |arr|
        arr.inject(true){|r, el| r && can?(:read, el)}
      end
      can   :perform, :quick_search
    end
  end
end