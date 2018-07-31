from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect, Http404
from enroll.models import student_info
from django.core.urlresolvers import reverse

def index(request):
	stu_list=student_info.objects.all()
	return render(request, 'enroll/index.html',{'stu_list':stu_list})
def create(request):
	if request.method == 'GET':
		return render(request, 'enroll/create.html')
	elif request.method == 'POST':
		student = student_info(student_name=request.POST['name'], student_age=request.POST['age'], student_gender = request.POST['gender'], student_major = request.POST['major'])
		student.save()
		return HttpResponseRedirect(reverse('enroll:detail', args=(student.id,)))
def detail(request, student_id):
	try:
		student=student_info.objects.get(pk=student_id)
	except student_info.DoesNotExist:
		raise Http404("student does not exist")
	return render(request, 'enroll/detail.html', {'student':student})

# Create your views here.
