# == Schema Information
#
# Table name: companies
#
#  id            :integer          not null, primary key
#  name          :string(255)
#  address       :string(255)
#  contact_email :string(255)
#  created_at    :datetime         not null
#  updated_at    :datetime         not null
#

class Company < ActiveRecord::Base
  attr_accessible :address, :contact_email, :name
end
