import React, { useState } from 'react';
import { Search, Filter, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { FilterConfig, SearchConfig } from '@/types';

interface FilterPanelProps {
  filters: FilterConfig[];
  searchConfig: SearchConfig;
  onFilterChange: (filters: Record<string, string>) => void;
  onSearch: (query: string, field: string) => void;
}

export const FilterPanel: React.FC<FilterPanelProps> = ({
  filters,
  searchConfig,
  onFilterChange,
  onSearch,
}) => {
  const [filterValues, setFilterValues] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchField, setSearchField] = useState(searchConfig.searchableFields[0]?.value || '');
  const [isExpanded, setIsExpanded] = useState(false);

  const handleFilterChange = (field: string, value: string) => {
    const newFilters = { ...filterValues, [field]: value };
    setFilterValues(newFilters);
    onFilterChange(newFilters);
  };

  const handleSearch = () => {
    onSearch(searchQuery, searchField);
  };

  const handleClearFilters = () => {
    setFilterValues({});
    setSearchQuery('');
    onFilterChange({});
    onSearch('', searchField);
  };

  const renderFilterInput = (filter: FilterConfig) => {
    switch (filter.type) {
      case 'select':
        return (
          <Select
            value={filterValues[filter.field] || ''}
            onValueChange={(value) => handleFilterChange(filter.field, value)}
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder={`Select ${filter.label}`} />
            </SelectTrigger>
            <SelectContent>
              {filter.options?.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        );
      case 'date':
        return (
          <Input
            type="date"
            value={filterValues[filter.field] || ''}
            onChange={(e) => handleFilterChange(filter.field, e.target.value)}
          />
        );
      case 'number-range':
        return (
          <div className="flex gap-2">
            <Input
              type="number"
              placeholder="Min"
              value={filterValues[`${filter.field}_min`] || ''}
              onChange={(e) => handleFilterChange(`${filter.field}_min`, e.target.value)}
              className="w-1/2"
            />
            <Input
              type="number"
              placeholder="Max"
              value={filterValues[`${filter.field}_max`] || ''}
              onChange={(e) => handleFilterChange(`${filter.field}_max`, e.target.value)}
              className="w-1/2"
            />
          </div>
        );
      case 'date-range':
        return (
          <div className="flex gap-2">
            <Input
              type="date"
              value={filterValues[`${filter.field}_from`] || ''}
              onChange={(e) => handleFilterChange(`${filter.field}_from`, e.target.value)}
              className="w-1/2"
            />
            <Input
              type="date"
              value={filterValues[`${filter.field}_to`] || ''}
              onChange={(e) => handleFilterChange(`${filter.field}_to`, e.target.value)}
              className="w-1/2"
            />
          </div>
        );
      default:
        return (
          <Input
            type="text"
            value={filterValues[filter.field] || ''}
            onChange={(e) => handleFilterChange(filter.field, e.target.value)}
            placeholder={`Enter ${filter.label}`}
          />
        );
    }
  };

  return (
    <div className="filter-panel animate-fade-in">
      {/* Search Bar */}
      <div className="flex gap-3 mb-4">
        <div className="flex-1 flex gap-2">
          <Select value={searchField} onValueChange={setSearchField}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Search by..." />
            </SelectTrigger>
            <SelectContent>
              {searchConfig.searchableFields.map((field) => (
                <SelectItem key={field.value} value={field.value}>
                  {field.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              type="text"
              placeholder="Search..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="pl-10"
            />
          </div>
          <Button onClick={handleSearch}>Search</Button>
        </div>
        <Button
          variant="outline"
          onClick={() => setIsExpanded(!isExpanded)}
          className="gap-2"
        >
          <Filter className="h-4 w-4" />
          Filters
        </Button>
        {Object.keys(filterValues).length > 0 && (
          <Button variant="ghost" onClick={handleClearFilters} className="gap-2">
            <X className="h-4 w-4" />
            Clear
          </Button>
        )}
      </div>

      {/* Filter Fields */}
      {isExpanded && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 pt-4 border-t border-border animate-slide-in">
          {filters.map((filter) => (
            <div key={filter.field} className="space-y-2">
              <Label className="text-sm font-medium">{filter.label}</Label>
              {renderFilterInput(filter)}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
