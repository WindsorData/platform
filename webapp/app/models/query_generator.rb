class QueryGenerator

  def self.json_query(params_hash)
    query = {executives: []}
    params_hash.each_with_index { |(key, value), index|
      build_role_query(index, key, params_hash, query)
    }
    
    # Clear values
    query.keys.each { |k|
      query.delete(k) if query[k].empty?
    }
    query[:executives].each_with_index { |(k, v), index|
      query[:executives][index][:executivesFilters].delete_if {|x| x.blank? }
      query[:executives][index].delete(:executivesFilters) if query[:executives][index][:executivesFilters].blank?
    }
    query[:executives].delete_if {|x| x.blank?}
    query.delete(:executives) if query[:executives].blank?
    if query.blank?
      "{}"
    else
      query.to_json.gsub(/(")(\d+)(")/, ' \2')
    end
  end

  def self.build_role_query(i1, k1, params_hash, query)
    query[:executives][i1] = {executivesFilters: [([])]}
    params_hash[k1].each_with_index { |(k2, v2), i2|
      if params_hash[k1][k2].is_a? Hash
        if !params_hash[k1][k2]['type'].blank?
          if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
            hash = {key: params_hash[k1][k2]['type'], operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']},
                                                                                      {operator: 'lt', value: params_hash[k1][k2]['lt']}]}
          elsif !params_hash[k1][k2]['gt'].blank?
            hash = {key: params_hash[k1][k2]['type'], operators: [{operator: 'gt', value: params_hash[k1][k2]['gt']}]}
          elsif !params_hash[k1][k2]['lt'].blank?
            hash = {key: params_hash[k1][k2]['type'], operators: [{operator: 'lt', value: params_hash[k1][k2]['lt']}]}
          end
        else
          if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2, operators: [{gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']}]}
          elsif !params_hash[k1][k2]['gt'].blank?
            hash = {key: k2, operators: [{gt: params_hash[k1][k2]['gt']}]}
          elsif !params_hash[k1][k2]['lt'].blank?
            hash = {key: k2, operators: [{lt: params_hash[k1][k2]['lt']}]}
          end
        end
      else
        hash = {key: k2, value: params_hash[k1][k2] == '_' ? "" : params_hash[k1][k2] } unless params_hash[k1][k2].blank? || params_hash[k1][k2] == 'N/A'
      end
      query[:executives][i1][:executivesFilters][i2] = hash if hash
    }
  end
end