# == Schema Information
#
# Table name: compensations
#
#  id         :integer          not null, primary key
#  field      :string(255)
#  value      :string(255)
#  created_at :datetime         not null
#  updated_at :datetime         not null
#  type       :string(255)
#

class EquityCompensation < Compensation

end
