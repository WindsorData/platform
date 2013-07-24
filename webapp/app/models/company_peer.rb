# == Schema Information
#
# Table name: company_peers
#
#  id         :integer          not null, primary key
#  ticker     :string(255)      not null
#  name       :string(255)      not null
#  created_at :datetime         not null
#  updated_at :datetime         not null
#

class CompanyPeer < ActiveRecord::Base
  attr_accessible :name, :ticker
end
