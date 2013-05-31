class Company < ActiveRecord::Base
  attr_accessible :address, :contact_email, :name
end
