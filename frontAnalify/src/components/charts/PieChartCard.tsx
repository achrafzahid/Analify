import React from 'react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

interface PieChartCardProps {
  title: string;
  description?: string;
  data: { name: string; value: number }[];
  colors?: string[];
  height?: number;
}

const DEFAULT_COLORS = [
  'hsl(217, 91%, 60%)',
  'hsl(142, 76%, 36%)',
  'hsl(48, 96%, 53%)',
  'hsl(271, 91%, 65%)',
  'hsl(0, 84%, 60%)',
  'hsl(173, 58%, 39%)',
  'hsl(24, 95%, 53%)',
];

export const PieChartCard: React.FC<PieChartCardProps> = ({
  title,
  description,
  data,
  colors = DEFAULT_COLORS,
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
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={100}
            paddingAngle={2}
            dataKey="value"
            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
            labelLine={false}
          >
            {data.map((_, index) => (
              <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              backgroundColor: 'hsl(var(--card))',
              border: '1px solid hsl(var(--border))',
              borderRadius: '8px',
              color: 'hsl(var(--foreground))',
            }}
          />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};
