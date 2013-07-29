class DetailUploadFile < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :file_name, :messages, :ticker
  belongs_to :ticker

end