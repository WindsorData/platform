# == Schema Information
#
# Table name: groups
#
#  id         :integer          not null, primary key
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  name       :string(255)
#

class Group < ActiveRecord::Base
  attr_accessible :name
end
