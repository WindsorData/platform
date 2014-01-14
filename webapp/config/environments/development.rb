WindosorFrontend::Application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  # In the development environment your application's code is reloaded on
  # every request. This slows down response time but is perfect for development
  # since you don't have to restart the web server when you make code changes.
  config.cache_classes = false

  # Log error messages when you accidentally call methods on nil.
  config.whiny_nils = true

  # Show full error reports and disable caching
  config.consider_all_requests_local       =  true
  config.action_controller.perform_caching = false

  # Don't care if the mailer can't send
  config.action_mailer.raise_delivery_errors = false

  # Print deprecation notices to the Rails logger
  config.active_support.deprecation = :log

  # Only use best-standards-support built into browsers
  config.action_dispatch.best_standards_support = :builtin

  # Raise exception on mass assignment protection for Active Record models
  config.active_record.mass_assignment_sanitizer = :strict

  # Log the query plan for queries taking more than this (works
  # with SQLite, MySQL, and PostgreSQL)
  config.active_record.auto_explain_threshold_in_seconds = 0.5

  # Do not compress assets
  config.assets.compress = false

  # Expands the lines which load the assets
  config.assets.debug = true

  # Backend params
  config.backend_host = 'http://localhost:9000'
  
  config.get_companies_path = '/api/companies'

  config.get_company_peers_path ='/api/companies/report/peers/tickers'
  config.post_incoming_peers_path = '/api/companies/report/peers/incoming'
  config.post_incoming_peers_raw_data_path = '/api/companies/report/peers/incoming/raw'
  config.post_peers_peers_single_ticker_path = '/api/companies/report/peers/peers'
  config.post_peers_peers_raw_data_path ="/api/companies/report/peers/peers/raw"
  config.post_peers_peers_ticker_list_raw_data_path ="/api/companies/report/peers/peersFromPrimary/raw"
  config.post_peers_peers_ticker_list_path = '/api/companies/report/peers/peersFromPrimary'

  # Get search form values
  config.get_primary_roles_path = '/api/schema/values/roles/primary'
  config.get_secondary_roles_path = '/api/schema/values/roles/secondary'  
  config.get_cash_compensations_path = '/api/schema/values/cashCompensations'
  config.get_equity_compensations_path = '/api/schema/values/equityCompensations'
  config.get_level_roles_path = '/api/schema/values/roles/level'
  config.get_scope_roles_path = '/api/schema/values/roles/scope'
  config.get_bod_roles_path = '/api/schema/values/roles/bod'
  
  # File upload paths
  config.post_top5_path = '/api/companies/top5'
  config.post_guidelines_path = '/api/companies/guidelines'
  config.post_dilution_path = '/api/companies/dilution'
  config.peers_path = '/api/companies/peers'
  config.drop_all_peers_path = '/api/companies/allPeers'
  config.post_bod_path = '/api/companies/bod'
  config.post_batch_companies_path = '/api/companies/batch/companies'
  config.post_batch_bod_path = '/api/companies/batch/bod'
  config.post_batch_peers_path = '/api/companies/batch/peers'

  # Search paths
  config.post_query_path = '/api/companies/search'
  config.post_download_top5_report_path = '/api/companies/report'
  config.post_download_bod_report_path = '/api/companies/report/bod'
  config.post_download_full_report_path = '/api/companies/report/full'
end
