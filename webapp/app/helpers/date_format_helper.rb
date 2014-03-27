module DateFormatHelper
  def to_PST(date)
    Time.parse(date.to_s)
      .in_time_zone("Pacific Time (US & Canada)")
      .strftime('%B %e at %l:%M %p')
  end
end