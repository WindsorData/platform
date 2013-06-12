# == Schema Information
#
# Table name: roles
#
#  id         :integer          not null, primary key
#  name       :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

class Role < ActiveRecord::Base
  attr_accessible :name

  validates :name, presence: true

  def self.load_json(json)
    Role.create(JSON.parse(json))
  end
end
