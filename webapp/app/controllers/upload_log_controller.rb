require 'active_support'

class UploadLogController < ApplicationController
  before_filter {authorize!(:audit, :upload_log) }

  def audit
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8)
    user_id = params_hash['user'].to_i unless params_hash['user'].blank?
    since = params_hash['date_since'].try(:to_date).try(:beginning_of_day)
    to = params_hash['date_to'].try(:to_date).try(:end_of_day)

    @logs = UploadLog
              .with_user(user_id)
              .created_since(since)
              .created_to(to)
              .order('created_at desc').paginated(params)
    @users = User.order('email desc')
  end
end
