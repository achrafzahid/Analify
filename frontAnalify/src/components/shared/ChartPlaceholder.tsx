import React from 'react';
import { BarChart3, LineChart, PieChart, TrendingUp } from 'lucide-react';

type ChartType = 'bar' | 'line' | 'pie' | 'area';

interface ChartPlaceholderProps {
  title: string;
  type?: ChartType;
  height?: number;
}

const chartIcons: Record<ChartType, React.ElementType> = {
  bar: BarChart3,
  line: LineChart,
  pie: PieChart,
  area: TrendingUp,
};

export const ChartPlaceholder: React.FC<ChartPlaceholderProps> = ({
  title,
  type = 'bar',
  height = 300,
}) => {
  const Icon = chartIcons[type];

  return (
    <div
      className="stat-card flex flex-col items-center justify-center animate-fade-in"
      style={{ height }}
    >
      <div className="p-4 bg-muted rounded-full mb-4">
        <Icon className="h-8 w-8 text-muted-foreground" />
      </div>
      <h3 className="text-lg font-semibold text-foreground mb-2">{title}</h3>
      <p className="text-sm text-muted-foreground text-center max-w-xs">
        Chart placeholder - Connect to backend to display real data
      </p>
      <div className="mt-4 flex gap-2">
        <div className="h-2 w-16 bg-primary/20 rounded" />
        <div className="h-2 w-12 bg-primary/30 rounded" />
        <div className="h-2 w-20 bg-primary/40 rounded" />
      </div>
    </div>
  );
};
