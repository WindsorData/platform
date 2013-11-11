module SearchHelper
  def log_search_path(search)
    case search.type
    when Top5Search.name
      top_5_recent_search_path(search)
    else
      search_log_path(search)
    end
  end
end