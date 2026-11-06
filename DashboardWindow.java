import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.*;
import service.*;

public final class DashboardWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color BG = Color.decode("#F5F1E8"), SIDE = Color.decode("#E8E5DE"), CARD = Color.decode("#FCFBF8");
    private static final Color SURFACE = Color.decode("#EFEBE3"), BORDER = Color.decode("#D8D3C9"), TEXT = Color.decode("#292A28");
    private static final Color MUTED = Color.decode("#85847F"), SAGE = Color.decode("#748B73"), AMBER = Color.decode("#C49A58"), BRICK = Color.decode("#B66B63");
    private static final Font BODY = new Font("Inter", Font.PLAIN, 13), MED = new Font("Inter", Font.BOLD, 13);
    private final transient CrewService crews = new CrewService(); private final transient FlightService flights = new FlightService();
    private final transient AssignmentService assignments = new AssignmentService(); private final transient LeaveService leaves = new LeaveService();
    private final JPanel content = new JPanel(new BorderLayout(0, 20)); private String page = "Dashboard";

    public DashboardWindow() {
        super("SkyLink-Flight Crew Scheduling System");
        UIManager.put("Panel.background", CARD); UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT); UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.foreground", TEXT); UIManager.put("TextField.background", CARD);
        UIManager.put("ComboBox.background", CARD); UIManager.put("Table.background", CARD);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setMinimumSize(new Dimension(1120, 720));
        setSize(1440, 900); setLocationRelativeTo(null); getContentPane().setBackground(BG);
        setLayout(new BorderLayout()); add(sidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout()); right.setBackground(BG); right.add(topbar(), BorderLayout.NORTH);
        content.setBackground(BG); content.setBorder(new EmptyBorder(28, 34, 30, 34)); right.add(content, BorderLayout.CENTER); add(right, BorderLayout.CENTER);
        showPage("Dashboard");
    }

    private JPanel sidebar() {
        JPanel p = new JPanel(); p.setPreferredSize(new Dimension(246, 0)); p.setBackground(SIDE); p.setBorder(new EmptyBorder(28, 18, 24, 18)); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("  CREW  /  OPS"); brand.setFont(new Font("Inter", Font.BOLD, 16)); brand.setForeground(TEXT); brand.setBorder(new EmptyBorder(4, 3, 36, 0)); p.add(brand);
        String[][] items = {{"OVERVIEW", "Dashboard"}, {"OPERATIONS", "Flights", "Assignments"}, {"PEOPLE", "Crew", "Leave"}, {"INSIGHTS", "Reports"}};
        for (String[] group : items) {
            JLabel cap = label(group[0], 10, MUTED, true); cap.setBorder(new EmptyBorder(14, 8, 9, 0)); p.add(cap);
            for (int i=1;i<group.length;i++) { String name=group[i]; JButton b=navButton(name); b.addActionListener(e -> showPage(name)); p.add(b); }
        }
        p.add(Box.createVerticalGlue()); JLabel foot=label("AIRLINE OPERATIONS",11,MUTED,false); foot.setBorder(new EmptyBorder(0,8,4,0)); p.add(foot); return p;
    }
    private JButton navButton(String name) {
        JButton b=new JButton("   "+name); b.setHorizontalAlignment(SwingConstants.LEFT); b.setFont(MED); b.setForeground(TEXT); b.setBackground(SIDE); b.setOpaque(true); b.setContentAreaFilled(true); b.setFocusPainted(false); b.setBorder(new EmptyBorder(12,7,12,7)); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,42)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){if(!name.equals(page))b.setBackground(SURFACE);}public void mouseExited(MouseEvent e){if(!name.equals(page))b.setBackground(SIDE);}});
        b.addActionListener(e -> { for(Component c:b.getParent().getComponents()) if(c instanceof JButton) { ((JButton)c).setBackground(SIDE); ((JButton)c).setForeground(TEXT); } b.setBackground(CARD); b.setForeground(SAGE); }); return b;
    }
    private JPanel topbar() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(CARD); p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER),new EmptyBorder(17,34,17,34)));
        JLabel left=label("OPERATIONS  /  "+page.toUpperCase(),11,MUTED,true); JLabel right=label(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),13,TEXT,false);
        p.add(left,BorderLayout.WEST); p.add(right,BorderLayout.EAST); return p;
    }
    private void showPage(String target) {
        page=target; getContentPane().remove(1); JPanel right=new JPanel(new BorderLayout()); right.setBackground(BG); right.add(topbar(),BorderLayout.NORTH);
        content.removeAll(); content.setLayout(new BorderLayout(0,20)); content.setBorder(new EmptyBorder(28,34,30,34)); right.add(content,BorderLayout.CENTER); add(right,BorderLayout.CENTER);
        try { switch(target) { case "Dashboard": dashboard(); break; case "Crew": tablePage("Crew directory","Manage crew profiles, contact details and availability.",crewTable(),"Add crew member",()->crewDialog(null)); break;
            case "Flights": tablePage("Flight schedule","Review scheduled departures, routes and time zones.",flightTable(),"Add flight",()->flightDialog(null)); break;
            case "Assignments": tablePage("Crew assignments","Assign eligible crew and review schedule compliance.",assignmentTable(),"Add crew",()->assignmentDialog(null)); break;
            case "Leave": tablePage("Leave requests","Review requests and manage crew availability.",leaveTable(),"Apply for leave",()->leaveDialog(null)); break; case "Reports": reports(); break; } }
        catch(Exception ex) { showError(ex); }
        revalidate(); repaint();
    }
    private void dashboard() throws Exception {
        title("Good day, Operations", "A clear view of today's crew and flight activity.");
        List<CrewMember> cs=crews.getAllCrew(); List<Flight> fs=flights.listFlights(); List<Assignment> as=assignments.viewAssignments(); List<Leave> ls=leaves.viewLeaves();
        JPanel stats=new JPanel(new GridLayout(1,4,16,0)); stats.setOpaque(false);
        stats.add(stat("Total crew",String.valueOf(cs.size()),"Crew profiles")); stats.add(stat("Scheduled flights",String.valueOf(fs.size()),"In the schedule"));
        stats.add(stat("Assignments",String.valueOf(as.size()),"Crew allocations")); stats.add(stat("Pending leave",String.valueOf(ls.stream().filter(l->"Pending".equals(l.getStatus())).count()),"Awaiting review"));
        JPanel center=new JPanel(new GridLayout(1,2,18,0)); center.setOpaque(false); center.add(chartCard(cs)); center.add(availabilityCard(cs));
        JPanel activity=card(); activity.setLayout(new BorderLayout(0,14)); activity.add(sectionHeader("Flight activity","Upcoming schedule"),BorderLayout.NORTH); activity.add(new JScrollPane(flightTable()),BorderLayout.CENTER);
        JPanel stack=new JPanel(new BorderLayout(0,18)); stack.setOpaque(false); stack.add(center,BorderLayout.NORTH); stack.add(activity,BorderLayout.CENTER);
        JPanel pageBody=new JPanel(new BorderLayout(0,22)); pageBody.setOpaque(false); pageBody.add(stats,BorderLayout.NORTH); pageBody.add(stack,BorderLayout.CENTER); content.add(pageBody);
    }
    private JPanel stat(String name,String value,String hint) { JPanel p=card(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.add(label(name,13,MUTED,false)); p.add(Box.createVerticalStrut(15)); JLabel n=label(value,30,TEXT,true); p.add(n); p.add(Box.createVerticalStrut(4)); p.add(label(hint,12,MUTED,false)); return p; }
    private JPanel chartCard(List<CrewMember> list) {
        JPanel p=card(); p.setLayout(new BorderLayout(0,16)); p.add(sectionHeader("Crew overview","Availability mix"),BorderLayout.NORTH);
        JPanel bars=new JPanel(new GridLayout(3,1,0,12)); bars.setOpaque(false); int avail=0, unavailable=0, leave=0; for(CrewMember c:list) { if("Available".equals(c.getAvailability())) avail++; else if("On Leave".equals(c.getAvailability())) leave++; else unavailable++; }
        bars.add(progress("Available",avail,list.size(),SAGE)); bars.add(progress("Unavailable",unavailable,list.size(),BRICK)); bars.add(progress("On Leave",leave,list.size(),AMBER)); p.add(bars,BorderLayout.CENTER); return p;
    }
    private JPanel progress(String name,int n,int total,Color color) { JPanel p=new JPanel(new BorderLayout(0,7)); p.setOpaque(false); JLabel l=label(name+"   "+n,12,TEXT,false); p.add(l,BorderLayout.NORTH); JProgressBar bar=new JProgressBar(0,Math.max(total,1)); bar.setValue(n); bar.setForeground(color); bar.setBackground(SURFACE); bar.setBorderPainted(false); bar.setPreferredSize(new Dimension(220,8)); p.add(bar,BorderLayout.CENTER); return p; }
    private JPanel availabilityCard(List<CrewMember> cs) { JPanel p=card(); p.setLayout(new BorderLayout(0,12)); p.add(sectionHeader("Crew availability","Current status"),BorderLayout.NORTH); JPanel rows=new JPanel(); rows.setOpaque(false); rows.setLayout(new BoxLayout(rows,BoxLayout.Y_AXIS)); int shown=0;
        for(CrewMember c:cs) if(shown++<6) { JPanel row=new JPanel(new BorderLayout()); row.setOpaque(false); row.setBorder(new EmptyBorder(8,0,8,0)); row.add(label(c.getName()+"   ·   "+c.getCrewType(),12,TEXT,false),BorderLayout.WEST); row.add(pill(c.getAvailability()),BorderLayout.EAST); rows.add(row); }
        if(cs.isEmpty())rows.add(label("No crew profiles yet.",13,MUTED,false)); p.add(rows,BorderLayout.CENTER); return p; }
    private JPanel reports() throws Exception { List<CrewMember> cs=crews.getAllCrew(); List<Flight> fs=flights.listFlights(); List<Assignment> as=assignments.viewAssignments(); JPanel upper=new JPanel();upper.setOpaque(false);upper.setLayout(new BoxLayout(upper,BoxLayout.Y_AXIS));upper.add(label("Reports",25,TEXT,true));upper.add(Box.createVerticalStrut(6));upper.add(label("A concise view of the current operation.",13,MUTED,false));upper.add(Box.createVerticalStrut(20));JPanel tiles=new JPanel(new GridLayout(1,3,16,0));tiles.setOpaque(false);tiles.add(stat("Crew members",String.valueOf(cs.size()),"All active profiles"));tiles.add(stat("Flights",String.valueOf(fs.size()),"All scheduled flights"));tiles.add(stat("Assignments",String.valueOf(as.size()),"Current crew allocations"));upper.add(tiles);content.add(upper,BorderLayout.NORTH); JTable table=assignmentTable(); JPanel c=card(); c.setLayout(new BorderLayout(0,12)); c.add(sectionHeader("Assignment report","Crew by scheduled flight"),BorderLayout.NORTH); c.add(new JScrollPane(table),BorderLayout.CENTER); content.add(c,BorderLayout.CENTER); return c; }

    private void tablePage(String heading,String subtitle,JTable table,String addLabel,Runnable add) {
        JPanel top=new JPanel(new BorderLayout(0,16)); top.setOpaque(false); JPanel copy=new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS)); copy.add(label(heading,25,TEXT,true)); copy.add(Box.createVerticalStrut(6)); copy.add(label(subtitle,13,MUTED,false));
        JButton create=primary(addLabel); create.addActionListener(e->{add.run();showPage(page);}); top.add(copy,BorderLayout.WEST); top.add(create,BorderLayout.EAST); content.add(top,BorderLayout.NORTH);
        JPanel c=card(); c.setLayout(new BorderLayout(0,14)); JPanel controls=new JPanel(new BorderLayout()); controls.setOpaque(false); JTextField search=field("");search.setToolTipText("Search this table");JPanel searchWrap=new JPanel(new BorderLayout(8,0));searchWrap.setOpaque(false);searchWrap.add(label("⌕",18,MUTED,false),BorderLayout.WEST);searchWrap.add(search,BorderLayout.CENTER);searchWrap.setPreferredSize(new Dimension(300,40));controls.add(searchWrap,BorderLayout.WEST);
        TableRowSorter<TableModel> sorter=new TableRowSorter<>(table.getModel());table.setRowSorter(sorter);List<Integer> searchable=new ArrayList<>();for(int i=0;i<table.getModel().getColumnCount();i++)if(!"__pk".equals(table.getModel().getColumnName(i)))searchable.add(i);int[] searchableColumns=searchable.stream().mapToInt(Integer::intValue).toArray();search.getDocument().addDocumentListener(new DocumentListener(){private void apply(){String q=search.getText().trim();sorter.setRowFilter(q.isEmpty()?null:RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(q),searchableColumns));}public void insertUpdate(DocumentEvent e){apply();}public void removeUpdate(DocumentEvent e){apply();}public void changedUpdate(DocumentEvent e){apply();}});
        JButton edit=secondary("Edit selected"), del=secondary("Delete selected"); JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actions.setOpaque(false); actions.add(edit); actions.add(del); controls.add(actions,BorderLayout.EAST);
        edit.addActionListener(e->{if(table.getSelectedRow()<0){notice("Select a row to edit.");return;} editSelected(table);});
        del.addActionListener(e->{if(table.getSelectedRow()<0){notice("Select a row to delete.");return;} deleteSelected(table);});
        styleTable(table); c.add(controls,BorderLayout.NORTH); c.add(new JScrollPane(table),BorderLayout.CENTER); content.add(c,BorderLayout.CENTER);
    }
    private JTable crewTable() { try { List<CrewMember> a=crews.getAllCrew(); Object[][] d=new Object[a.size()][8]; for(int i=0;i<a.size();i++){CrewMember c=a.get(i);d[i]=new Object[]{crewNo(c.getCrewId()),c.getName(),c.getCrewType(),c.getPhone(),c.getEmail(),c.getGender(),c.getAvailability(),c.getCrewId()};}return new JTable(new DefaultTableModel(d,new String[]{"Crew No.","Name","Role","Phone","Email","Gender","Availability","__pk"}){public boolean isCellEditable(int r,int c){return false;}}); }catch(Exception e){showError(e);return emptyTable("Crew No.","Name");} }
    private JTable flightTable() { try { List<Flight>a=flights.listFlights();Object[][]d=new Object[a.size()][7];for(int i=0;i<a.size();i++){Flight f=a.get(i);d[i]=new Object[]{f.getFlightNumber(),f.getSource()+" → "+f.getDestination(),f.getDepartureTime().toString().replace('T',' '),f.getArrivalTime().toString().replace('T',' '),f.getSourceZoneId(),f.getDestinationZoneId(),f.getFlightId()};}return new JTable(new DefaultTableModel(d,new String[]{"Flight No.","Route","Departure (local)","Arrival (local)","Origin zone","Destination zone","__pk"}){public boolean isCellEditable(int r,int c){return false;}});}catch(Exception e){showError(e);return emptyTable("Flight No.","Route");} }
    private JTable assignmentTable() { try { List<Assignment>a=assignments.viewAssignments();Map<Integer,CrewMember> crewMap=new HashMap<>();for(CrewMember c:crews.getAllCrew())crewMap.put(c.getCrewId(),c);Map<Integer,Flight> flightMap=new HashMap<>();for(Flight f:flights.listFlights())flightMap.put(f.getFlightId(),f);Object[][]d=new Object[a.size()][6];for(int i=0;i<a.size();i++){Assignment x=a.get(i);CrewMember c=crewMap.get(x.getCrewId());Flight f=flightMap.get(x.getFlightId());d[i]=new Object[]{c==null?"":crewNo(c.getCrewId()),c==null?"":c.getName(),f==null?"":f.getFlightNumber(),f==null?"":f.getSource()+" → "+f.getDestination(),x.getAssignmentDate(),x.getAssignmentId()};}return new JTable(new DefaultTableModel(d,new String[]{"Crew No.","Crew member","Flight No.","Route","Assignment date","__pk"}){public boolean isCellEditable(int r,int c){return false;}});}catch(Exception e){showError(e);return emptyTable("Crew No.","Flight No.");} }
    private JTable leaveTable() { try { List<Leave>a=leaves.viewLeaves();Map<Integer,CrewMember> crewMap=new HashMap<>();for(CrewMember c:crews.getAllCrew())crewMap.put(c.getCrewId(),c);Object[][]d=new Object[a.size()][7];for(int i=0;i<a.size();i++){Leave l=a.get(i);CrewMember c=crewMap.get(l.getCrewId());d[i]=new Object[]{crewNo(l.getCrewId()),c==null?"":c.getName(),l.getFromDate(),l.getToDate(),l.getReason(),l.getStatus(),l.getLeaveId()};}return new JTable(new DefaultTableModel(d,new String[]{"Crew No.","Crew member","From","To","Reason","Status","__pk"}){public boolean isCellEditable(int r,int c){return false;}});}catch(Exception e){showError(e);return emptyTable("Crew No.","From");} }
    private JTable emptyTable(String... cols){return new JTable(new DefaultTableModel(new Object[0][cols.length],cols){public boolean isCellEditable(int r,int c){return false;}});}

    private void editSelected(JTable t) { try {int row=t.convertRowIndexToModel(t.getSelectedRow()),id=selectedPrimaryKey(t,row);switch(page){case "Crew":crewDialog(crews.getCrewById(id));break;case "Flights":flightDialog(flights.getFlightById(id));break;case "Assignments":assignmentDialog(assignments.assignmentById(id));break;case "Leave":leaveDialog(leaves.leaveById(id));break;}}catch(Exception e){showError(e);} }
    private void deleteSelected(JTable t) { int row=t.convertRowIndexToModel(t.getSelectedRow()); if(!confirmDelete())return;
        try{int id=selectedPrimaryKey(t,row);switch(page){case "Crew":crews.deleteCrew(id);break;case "Flights":flights.deleteFlight(id);break;case "Assignments":assignments.removeAssignment(id);break;case "Leave":leaves.deleteLeave(id);break;}showPage(page);}catch(Exception e){showError(e);} }
    private int selectedPrimaryKey(JTable t,int modelRow){int column=-1;for(int i=0;i<t.getModel().getColumnCount();i++)if("__pk".equals(t.getModel().getColumnName(i))){column=i;break;}if(column<0)throw new IllegalStateException("Internal record key is unavailable.");return ((Number)t.getModel().getValueAt(modelRow,column)).intValue();}

    private void crewDialog(CrewMember old) { boolean editing=old!=null; JTextField name=field(editing?old.getName():""), phone=field(editing?old.getPhone():""), email=field(editing?old.getEmail():""), gender=field(editing?old.getGender():""); JComboBox<String> role=new JComboBox<>(new String[]{"Pilot","Co-Pilot","Cabin Crew"}), availability=new JComboBox<>(new String[]{"Available","Unavailable","On Leave"}); if(editing){role.setSelectedItem(old.getCrewType());availability.setSelectedItem(old.getAvailability());}
        JPanel form=form("Crew profile","Enter crew details. The same form is used for add and edit.",new String[]{"Name","Role","Phone","Email","Gender (optional)","Availability"},new JComponent[]{name,role,phone,email,gender,availability}); if(form==null)return;
        try{CrewMember c=role.getSelectedItem().equals("Pilot")?new Pilot():role.getSelectedItem().equals("Co-Pilot")?new CoPilot():new CabinCrew();c.setName(name.getText());c.setRole((String)role.getSelectedItem());c.setPhone(phone.getText());c.setEmail(email.getText());c.setGender(gender.getText());c.setAvailability((String)availability.getSelectedItem());if(editing){c.setCrewId(old.getCrewId());crews.updateCrew(c);}else crews.addCrew(c);}catch(Exception e){showError(e);return;} showPage(page); }
    private void flightDialog(Flight old) {
        boolean editing=old!=null;
        JTextField number=field(editing?old.getFlightNumber():"");
        JTextField source=field(editing?old.getSource():"");
        JTextField dest=field(editing?old.getDestination():"");
        JTextField dep=field(editing?old.getDepartureTime().toString().replace('T',' '):"");
        JTextField arr=field(editing?old.getArrivalTime().toString().replace('T',' '):"");
        JTextField sz=field(editing?old.getSourceZoneId():"");
        JTextField dz=field(editing?old.getDestinationZoneId():"");
        JPanel form=form("Flight details","Enter locations and valid IANA time-zone identifiers manually, such as Asia/Kolkata.",new String[]{"Flight number","Source airport/location","Destination airport/location","Departure (yyyy-MM-dd HH:mm)","Arrival (yyyy-MM-dd HH:mm)","Source time zone","Destination time zone"},new JComponent[]{number,source,dest,dep,arr,sz,dz});
        if(form==null)return;
        try{
            Flight f=new Flight();
            f.setFlightNumber(number.getText().trim());
            f.setSource(source.getText().trim());
            f.setDestination(dest.getText().trim());
            f.setDepartureTime(LocalDateTime.parse(dep.getText().trim().replace(' ','T')));
            f.setArrivalTime(LocalDateTime.parse(arr.getText().trim().replace(' ','T')));
            f.setSourceZoneId(sz.getText().trim());
            f.setDestinationZoneId(dz.getText().trim());
            if(editing){f.setFlightId(old.getFlightId());flights.updateFlight(f);}else flights.addFlight(f);
        }catch(DateTimeParseException e){showError(new IllegalArgumentException("Enter flight times as yyyy-MM-dd HH:mm."));return;}
        catch(Exception e){showError(e);return;}
        showPage(page);
    }
    private void assignmentDialog(Assignment old) {
        boolean editing=old!=null;
        try{
            List<CrewMember> crewOptions=crews.getAllCrew();
            List<Flight> flightOptions=flights.listFlights();
            CrewMember oldCrew=crewOptions.stream().filter(c->editing&&c.getCrewId()==old.getCrewId()).findFirst().orElse(null);
            Flight oldFlight=flightOptions.stream().filter(f->editing&&f.getFlightId()==old.getFlightId()).findFirst().orElse(null);
            List<String> crewLabels=crewOptions.stream().map(c->crewNo(c.getCrewId())+" - "+c.getName()+" - "+c.getCrewType()).collect(java.util.stream.Collectors.toList());
            List<String> flightLabels=flightOptions.stream().map(f->f.getFlightNumber()+" - "+f.getSource()+" to "+f.getDestination()).collect(java.util.stream.Collectors.toList());
            JComboBox<String> crewBox=new JComboBox<>(crewLabels.toArray(new String[0]));
            JComboBox<String> flightBox=new JComboBox<>(flightLabels.toArray(new String[0]));
            if(oldCrew!=null)crewBox.setSelectedIndex(crewOptions.indexOf(oldCrew));
            if(oldFlight!=null)flightBox.setSelectedIndex(flightOptions.indexOf(oldFlight));
            JTextField date=field(editing?old.getAssignmentDate().toString():LocalDate.now().toString());
            JPanel form=form("Crew assignment","Eligibility, schedule conflicts and rest rules are checked before saving.",new String[]{"Crew member","Flight","Assignment date (yyyy-MM-dd)"},new JComponent[]{crewBox,flightBox,date});
            if(form==null)return;
            int crewIndex=crewBox.getSelectedIndex(), flightIndex=flightBox.getSelectedIndex();
            if(crewIndex<0||flightIndex<0)throw new IllegalArgumentException("Select a crew member and flight.");
            Assignment a=new Assignment(editing?old.getAssignmentId():0,crewOptions.get(crewIndex).getCrewId(),flightOptions.get(flightIndex).getFlightId(),LocalDate.parse(date.getText().trim()));
            if(editing)assignments.updateAssignment(a);else assignments.assignCrew(a);
        }catch(Exception e){showError(e);return;}
        showPage(page);
    }
    private void leaveDialog(Leave old) {
        boolean editing=old!=null;
        try{
            List<CrewMember> crewOptions=crews.getAllCrew();
            CrewMember oldCrew=crewOptions.stream().filter(c->editing&&c.getCrewId()==old.getCrewId()).findFirst().orElse(null);
            List<String> crewLabels=crewOptions.stream().map(c->crewNo(c.getCrewId())+" - "+c.getName()+" - "+c.getCrewType()).collect(java.util.stream.Collectors.toList());
            JComboBox<String> crew=new JComboBox<>(crewLabels.toArray(new String[0]));
            if(oldCrew!=null)crew.setSelectedIndex(crewOptions.indexOf(oldCrew));
            JTextField from=field(editing?old.getFromDate().toString():"");
            JTextField to=field(editing?old.getToDate().toString():"");
            JTextField reason=field(editing?old.getReason():"");
            JComboBox<String> status=new JComboBox<>(new String[]{"Pending","Approved","Rejected"});
            if(editing)status.setSelectedItem(old.getStatus());
            JPanel form=form("Leave request","Submit or update a crew leave request.",new String[]{"Crew member","From date (yyyy-MM-dd)","To date (yyyy-MM-dd)","Reason","Status"},new JComponent[]{crew,from,to,reason,status});
            if(form==null)return;
            int crewIndex=crew.getSelectedIndex();
            if(crewIndex<0)throw new IllegalArgumentException("Select a crew member.");
            CrewMember selected=crewOptions.get(crewIndex);
            Leave l=new Leave(editing?old.getLeaveId():0,selected.getCrewId(),LocalDate.parse(from.getText().trim()),LocalDate.parse(to.getText().trim()),reason.getText(),(String)status.getSelectedItem());
            if(editing)leaves.updateLeave(l);else leaves.applyLeave(l);
        }catch(Exception e){showError(e);return;}
        showPage(page);
    }
    private JPanel form(String heading,String subtitle,String[] names,JComponent[] fields) { JDialog d=new JDialog(this,heading,true);d.setUndecorated(true);JPanel wrap=card();wrap.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(25,28,22,28)));wrap.setLayout(new BorderLayout(0,20));JPanel head=new JPanel();head.setOpaque(false);head.setLayout(new BoxLayout(head,BoxLayout.Y_AXIS));head.add(label(heading,21,TEXT,true));head.add(Box.createVerticalStrut(6));head.add(label(subtitle,12,MUTED,false));wrap.add(head,BorderLayout.NORTH);JPanel grid=new JPanel(new GridBagLayout());grid.setOpaque(false);GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,0,7,12);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0;for(int i=0;i<names.length;i++){g.gridx=0;g.gridy=i;grid.add(label(names[i],12,TEXT,false),g);g.gridx=1;g.weightx=1;styleField(fields[i]);grid.add(fields[i],g);g.weightx=0;}wrap.add(grid,BorderLayout.CENTER);JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));buttons.setOpaque(false);JButton cancel=secondary("Cancel"),save=primary("Save changes");buttons.add(cancel);buttons.add(save);wrap.add(buttons,BorderLayout.SOUTH);final boolean[] ok={false};cancel.addActionListener(e->d.dispose());save.addActionListener(e->{ok[0]=true;d.dispose();});d.setContentPane(wrap);d.pack();d.setSize(Math.max(d.getWidth(),480),Math.min(Math.max(d.getHeight(),300),620));d.setLocationRelativeTo(this);d.setVisible(true);return ok[0]?wrap:null; }

    private void title(String h,String sub){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(label(h,25,TEXT,true));p.add(Box.createVerticalStrut(6));p.add(label(sub,13,MUTED,false));content.add(p,BorderLayout.NORTH);}
    private JPanel card(){JPanel p=new JPanel();p.setBackground(CARD);p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(20,21,20,21)));return p;}
    private JPanel sectionHeader(String h,String sub){JPanel p=new JPanel(new BorderLayout());p.setOpaque(false);JLabel a=label(h,16,TEXT,true);JLabel b=label(sub,12,MUTED,false);p.add(a,BorderLayout.WEST);p.add(b,BorderLayout.EAST);return p;}
    private JLabel label(String s,int size,Color color,boolean bold){JLabel l=new JLabel(s);l.setFont(new Font("Inter",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(color);return l;}
    private JTextField field(String s){JTextField f=new JTextField(s);styleField(f);return f;}
    private void styleField(JComponent c){c.setFont(BODY);c.setForeground(TEXT);c.setBackground(CARD);c.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(9,10,9,10)));c.setPreferredSize(new Dimension(230,38));c.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){c.setBorder(new CompoundBorder(new LineBorder(SAGE,1,true),new EmptyBorder(9,10,9,10)));}public void focusLost(FocusEvent e){c.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true),new EmptyBorder(9,10,9,10)));}});}
    private JButton primary(String s){JButton b=new JButton(s);b.setFont(MED);b.setForeground(CARD);b.setBackground(SAGE);b.setFocusPainted(false);b.setBorder(new EmptyBorder(11,17,11,17));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));Color base=SAGE;b.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){b.setBackground(base.darker());}public void mouseExited(MouseEvent e){b.setBackground(base);}});return b;}
    private JButton secondary(String s){JButton b=new JButton(s);b.setFont(MED);b.setForeground(TEXT);b.setBackground(SURFACE);b.setFocusPainted(false);b.setBorder(new EmptyBorder(9,13,9,13));Color base=SURFACE;b.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){b.setBackground(new Color(224,220,211));}public void mouseExited(MouseEvent e){b.setBackground(base);}});return b;}
    private JPanel pill(String s){Color c="Available".equals(s)?SAGE:"On Leave".equals(s)||"Pending".equals(s)?AMBER:BRICK;JPanel p=new JPanel();p.setBackground(new Color(c.getRed(),c.getGreen(),c.getBlue(),32));p.setBorder(new EmptyBorder(4,9,4,9));p.add(label(s,11,c,true));return p;}
    private void styleTable(JTable t){t.setRowHeight(43);t.setFont(BODY);t.setForeground(TEXT);t.setBackground(CARD);t.setGridColor(SURFACE);t.setShowVerticalLines(false);t.setSelectionBackground(new Color(116,139,115,45));t.setSelectionForeground(TEXT);t.setFillsViewportHeight(true);for(int i=t.getColumnModel().getColumnCount()-1;i>=0;i--){TableColumn col=t.getColumnModel().getColumn(i);if("__pk".equals(t.getModel().getColumnName(col.getModelIndex())))t.removeColumn(col);}JTableHeader h=t.getTableHeader();h.setFont(MED);h.setForeground(MUTED);h.setBackground(SURFACE);h.setPreferredSize(new Dimension(1,42));h.setBorder(new MatteBorder(0,0,1,0,BORDER));}
    private String crewNo(int id){return String.format(Locale.ROOT,"CR-%05d",id);}
    private void showError(Exception e){String m=e.getMessage()==null?"Operation failed.":e.getMessage();if(e instanceof java.sql.SQLException){int code=((java.sql.SQLException)e).getErrorCode();if(code==1451||code==1452)m="This record is linked to other records and cannot be changed. Review its assignments and leave requests, then try again.";}JOptionPane.showMessageDialog(this,m,"Please review",JOptionPane.ERROR_MESSAGE);}
    private void notice(String s){JOptionPane.showMessageDialog(this,s,"Crew Management",JOptionPane.INFORMATION_MESSAGE);}
    private boolean confirmDelete(){JDialog d=new JDialog(this,"Confirm deletion",true);d.setUndecorated(true);JPanel p=card();p.setBorder(new CompoundBorder(new LineBorder(BORDER),new EmptyBorder(23,25,20,25)));p.setLayout(new BorderLayout(0,16));JPanel copy=new JPanel();copy.setOpaque(false);copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));copy.add(label("Delete this "+page.toLowerCase()+" record?",18,TEXT,true));copy.add(Box.createVerticalStrut(8));String impact="Crew".equals(page)?"Linked assignments and leave requests will also be removed.":"Flights".equals(page)?"Crew assignments for this flight will also be removed.":"This action cannot be undone.";copy.add(label(impact,12,MUTED,false));p.add(copy,BorderLayout.CENTER);JPanel row=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));row.setOpaque(false);JButton no=secondary("Cancel"),yes=primary("Delete record");yes.setBackground(BRICK);row.add(no);row.add(yes);p.add(row,BorderLayout.SOUTH);final boolean[] confirmed={false};no.addActionListener(e->d.dispose());yes.addActionListener(e->{confirmed[0]=true;d.dispose();});d.setContentPane(p);d.pack();d.setLocationRelativeTo(this);d.setVisible(true);return confirmed[0];}
}
