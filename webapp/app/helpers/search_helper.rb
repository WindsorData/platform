module SearchHelper
  def log_search_path(search)
    case search.report_type
    when Constants::TOP5_REPORT
      top_5_recent_search_path(search)
    else
      search_log_path(search)
    end
  end
end