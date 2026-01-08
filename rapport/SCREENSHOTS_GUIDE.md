# How to Add Screenshots to the Report

## Overview
I've created a comprehensive screenshots chapter (`rapport/chapters/screenshots.tex`) with **placeholder positions** for all application screens. You just need to replace the placeholders with your actual screenshots.

## File Location
- **Chapter file**: `/home/ash/Desktop/analifyProject/rapport/chapters/screenshots.tex`
- **Images folder**: Create folder `/home/ash/Desktop/analifyProject/rapport/Images/` for screenshots

## Screenshot Placeholders Created

### 1. Landing & Authentication (Figures 8.1-8.2)
- `fig:landing-page` - Landing page with features presentation
- `fig:login-page` - Login form with email/password

### 2. Dashboards (Figures 8.3-8.5)
- `fig:dashboard-admin-global` - Admin global dashboard with all KPIs
- `fig:dashboard-admin-store` - Store manager dashboard (filtered)
- `fig:dashboard-investor` - Investor dashboard with portfolio

### 3. Advanced Analytics (Figures 8.6-8.7)
- `fig:enhanced-stats` - Enhanced analytics with interactive charts
- `fig:filter-panel` - Filter panel with date/store/product selectors

### 4. Products Management (Figures 8.8-8.9)
- `fig:products-list` - Products list with search and filters
- `fig:product-details` - Detailed product view with statistics

### 5. Orders Management (Figures 8.10-8.11)
- `fig:orders-list` - Orders list with status tracking
- `fig:create-order` - Create order form for cashiers

### 6. Bidding System (Figures 8.12-8.15)
- `fig:bidding-categories` - Categories navigation grid
- `fig:bidding-sections` - Available sections list
- `fig:place-bid` - Place bid form
- `fig:my-bids` - User's bids tracking

### 7. LLM Assistant (Figures 8.16-8.17)
- `fig:llm-assistant` - Chat interface with conversation
- `fig:example-questions` - Suggested questions panel

### 8. Users Management (Figures 8.18-8.19)
- `fig:users-management` - Users list with roles
- `fig:user-profile` - User profile edit form

### 9. Mobile Views (Figures 8.20-8.21)
- `fig:mobile-dashboard` - Responsive dashboard on smartphone
- `fig:mobile-assistant` - Mobile LLM assistant

### 10. Exports & Notifications (Figures 8.22-8.25)
- `fig:pdf-report` - Generated PDF report
- `fig:csv-export` - CSV export button
- `fig:error-messages` - Error notifications
- `fig:notifications` - Toast notifications system

## How to Add Your Screenshots

### Step 1: Take Screenshots
Capture screenshots of your running application for each feature listed above.

### Step 2: Save Images
Save your screenshots in the Images folder with descriptive names:
```bash
mkdir -p /home/ash/Desktop/analifyProject/rapport/Images

# Example naming:
# Images/landing-page.png
# Images/login-page.png
# Images/dashboard-admin-global.png
# Images/products-list.png
# ... etc
```

### Step 3: Replace Placeholders
In `rapport/chapters/screenshots.tex`, find lines like:
```latex
\begin{figure}[H]
	\centering
	% TODO: Insérer la capture d'écran de la landing page
	\fbox{\parbox{0.9\textwidth}{\centering \textit{[Screenshot: Landing Page - Vue d'ensemble avec présentation des fonctionnalités principales]}}}
	\caption{Landing Page - Interface d'accueil de la plateforme Analify}
	\label{fig:landing-page}
\end{figure}
```

Replace with:
```latex
\begin{figure}[H]
	\centering
	\includegraphics[width=0.9\textwidth]{Images/landing-page.png}
	\caption{Landing Page - Interface d'accueil de la plateforme Analify}
	\label{fig:landing-page}
\end{figure}
```

**Simply change:**
```latex
% TODO: Insérer la capture d'écran de la landing page
\fbox{\parbox{0.9\textwidth}{\centering \textit{[Screenshot: Landing Page...]}}}
```

**To:**
```latex
\includegraphics[width=0.9\textwidth]{Images/landing-page.png}
```

### Step 4: Compile PDF
```bash
cd /home/ash/Desktop/analifyProject/rapport
pdflatex main.tex
biber main
pdflatex main.tex
pdflatex main.tex
```

## Image Format Guidelines

### Recommended Formats
- **PNG** - Best for UI screenshots (lossless, transparent backgrounds)
- **JPEG** - OK for photos/large images (smaller file size)
- **PDF** - Perfect for vector graphics/diagrams

### Image Quality
- **Resolution**: 1920x1080 minimum for desktop views
- **DPI**: 150-300 DPI for print quality
- **Size**: Keep individual images under 2MB

### Screenshot Tips
1. **Clean the interface**: Close unnecessary tabs/windows
2. **Consistent browser zoom**: Use 100% zoom level
3. **Hide sensitive data**: Blur/mask any real user data
4. **Full-page captures**: Use browser extensions for scrolling captures
5. **Highlight features**: Use arrows/boxes to emphasize important elements (optional)

## Quick Replace Script (Optional)

Create a script to automate placeholder replacement:

```bash
#!/bin/bash
# replace-screenshots.sh

cd /home/ash/Desktop/analifyProject/rapport/chapters

# Replace all TODO comments with actual images
sed -i 's|% TODO: Insérer la capture.*\n.*\fbox.*|\\includegraphics[width=0.9\\textwidth]{Images/IMAGENAME.png}|g' screenshots.tex
```

## Testing Before Full Replacement

Test with ONE screenshot first:
1. Take screenshot of landing page
2. Save as `Images/landing-page.png`
3. Replace ONLY the first placeholder
4. Compile PDF with `pdflatex main.tex`
5. Check if image appears correctly
6. If good, proceed with all others

## Image Width Options

Adjust image size by changing the width parameter:
- `[width=0.9\textwidth]` - 90% of page width (default)
- `[width=1.0\textwidth]` - Full page width
- `[width=0.5\textwidth]` - Half page width (for mobile screenshots)
- `[scale=0.8]` - 80% of original size
- `[height=10cm]` - Fixed height

Example for mobile screenshots:
```latex
\includegraphics[width=0.5\textwidth]{Images/mobile-dashboard.png}
```

## Final Checklist

- [ ] Create `rapport/Images/` folder
- [ ] Take all 25 screenshots
- [ ] Name images descriptively
- [ ] Replace all TODO placeholders
- [ ] Compile PDF and verify images
- [ ] Check page breaks and layout
- [ ] Adjust image sizes if needed
- [ ] Final PDF compilation (3 times for references)

## Chapter Structure Summary

The screenshots chapter includes:
- **14 sections** covering all major features
- **25 figure placeholders** ready for your images
- **Detailed descriptions** for each screenshot
- **Technical context** explaining what's shown
- **Role-specific views** (Admin, Store Manager, Investor, Cashier)
- **Mobile responsiveness** demonstration

Each screenshot has:
- Clear caption
- Unique label for cross-referencing
- Descriptive text explaining visible elements
- Technical details about the feature
- User experience highlights

## Need Help?

If you encounter LaTeX compilation errors:
1. Check image file exists in `Images/` folder
2. Verify file extension matches (.png, .jpg, .pdf)
3. Check for special characters in filename (avoid spaces)
4. Ensure images aren't corrupted
5. Try compiling with `pdflatex -interaction=nonstopmode main.tex` to see all errors

Your report is now ready for screenshots! Just replace the placeholders with your actual images. 📸
