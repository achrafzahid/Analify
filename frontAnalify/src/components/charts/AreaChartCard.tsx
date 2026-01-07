import React from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

interface AreaChartCardProps {
  title: string;
  description?: string;
  data: Record<string, unknown>[];
  xKey: string;
  yKeys: string[];
  colors?: string[];
  height?: number;
}

export const AreaChartCard: React.FC<AreaChartCardProps> = ({
  title,
  description,
  data,
  xKey,
  yKeys,
  colors = ['hsl(217, 91%, 60%)'],
  height = 300,
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="stat-card animate-fade-in p-6">
        <h3 className="text-lg font-semibold text-foreground mb-2">{title}</h3>
        {description && <p className="text-sm text-muted-foreground mb-4">{description}</p>}
        <p className="text-sm text-muted-foreground">No data available</p>
      </div>
    );
  }
  
  return (
    <div className="stat-card animate-fade-in p-6">
      <h3 className="text-lg font-semibold text-foreground mb-2">{title}</h3>
      {description && <p className="text-sm text-muted-foreground mb-4">{description}</p>}
      <ResponsiveContainer width="100%" height={height}>
        <AreaChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
          <XAxis 
            dataKey={xKey} 
            tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
            stroke="hsl(var(--border))"
          />
          <YAxis 
            tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
            stroke="hsl(var(--border))"
          />
          <Tooltip
            contentStyle={{
              backgroundColor: 'hsl(var(--card))',
              border: '1px solid hsl(var(--border))',
              borderRadius: '8px',
              color: 'hsl(var(--foreground))',
            }}
          />
          <Legend />
          {yKeys.map((key, index) => (
            <Area
              key={key}
              type="monotone"
              dataKey={key}
              stroke={colors[index % colors.length]}
              fill={colors[index % colors.length]}
              fillOpacity={0.3}
              strokeWidth={2}
            />
          ))}
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
};
